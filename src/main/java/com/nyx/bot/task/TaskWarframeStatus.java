package com.nyx.bot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.application.NotificationApplicationService;
import com.nyx.bot.modules.warframe.repo.NotificationHistoryRepository;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.Cycle;
import io.github.kingprimes.model.worldstate.DateField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Component
@Slf4j
public class TaskWarframeStatus {

    static final long MIN_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    static final long MAX_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10);
    static final long BUFFER_MS = TimeUnit.MINUTES.toMillis(2);

    private final ObjectMapper objectMapper;
    private final NotificationApplicationService notificationService;
    private final NotificationHistoryRepository historyRepository;
    private final Environment environment;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledFuture;

    public TaskWarframeStatus(ObjectMapper objectMapper,
                              NotificationApplicationService notificationService,
                              NotificationHistoryRepository historyRepository,
                              Environment environment,
                              ScheduledExecutorService scheduler) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.historyRepository = historyRepository;
        this.environment = environment;
        this.scheduler = scheduler;
    }

    /**
     * 启动动态轮询调度。
     * 由 WarframeDataSource.init() 在数据初始化完成后调用。
     */
    public void startSchedule() {
        scheduler.schedule(this::executeWarframeStatus, 5, TimeUnit.SECONDS);
        log.info("动态轮询调度已启动，首次执行延迟 5 秒");
    }

    public void executeWarframeStatus() {
        if (!running.compareAndSet(false, true)) {
            log.debug("上一次轮询尚未完成，跳过本次");
            return;
        }
        try {
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
            if (body.code().is2xxSuccessful() || body.code().is3xxRedirection()) {
                try {
                    WorldState newState = objectMapper.readValue(body.body(), WorldState.class);
                    WorldState oldState;
                    try {
                        oldState = WarframeCache.getWarframeStatus();
                    } catch (DataNotInfoException e) {
                        oldState = newState;
                    }
                    notificationService.handleStateUpdate(oldState, newState);

                    List<Instant> expiries = collectExpiryTimestamps(newState);
                    long nextDelaySeconds = calculateNextDelay(expiries);
                    long ttlSeconds = nextDelaySeconds + TimeUnit.MILLISECONDS.toSeconds(BUFFER_MS);

                    WarframeCache.setWarframeStatus(newState, ttlSeconds);

                    log.debug("Warframe 状态数据更新成功，下次轮询: {}s 后", nextDelaySeconds);
                    scheduleNext(nextDelaySeconds);
                } catch (Exception e) {
                    log.error("Warframe 状态数据解析错误: {}", e.getMessage());
                    scheduleNext(TimeUnit.MILLISECONDS.toSeconds(MAX_INTERVAL_MS));
                }
            } else {
                log.error("Warframe 状态数据错误Code: {} - Body:{}", body.code(), body.body());
                scheduleNext(TimeUnit.MILLISECONDS.toSeconds(MAX_INTERVAL_MS));
            }
        } finally {
            running.set(false);
        }
    }

    /**
     * 安全获取 DateField 的 epochSecond。
     * DateField.getEpochSecond() 内部直接解引用 date 字段，
     * date 为 null 时会抛 NPE 而非返回 null，因此需要 try-catch 保护。
     */
    Instant safeEpochSecond(DateField df) {
        try {
            return df != null ? df.getEpochSecond() : null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    List<Instant> collectExpiryTimestamps(WorldState ws) {
        List<Instant> expiries = new ArrayList<>();

        Stream.of(ws.getAlerts(), ws.getActiveMissions(), ws.getConquests(),
                        ws.getDescents(), ws.getDailyDeals(), ws.getInvasions(),
                        ws.getVoidTraders(), ws.getVoidStorms(), ws.getSyndicateMissions())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(item -> safeEpochSecond(item.getExpiry()))
                .filter(Objects::nonNull)
                .forEach(expiries::add);

        // B. 周期与轮换类（实现 Cycle 接口，getExpiry() 直接返回 Instant）
        Stream.of(ws.getCetusCycle(), ws.getEarthCycle(), ws.getCambionCycle(),
                        ws.getVallisCycle(), ws.getZarimanCycle(), ws.getDuvalierCycle(),
                        ws.getSteelPath())
                .filter(Objects::nonNull)
                .map(Cycle::getExpiry)
                .filter(Objects::nonNull)
                .forEach(expiries::add);

        Instant now = Instant.now();
        expiries.removeIf(expiry -> !expiry.isAfter(now));

        return expiries;
    }

    long calculateNextDelay(List<Instant> expiries) {
        if (expiries.isEmpty()) {
            return TimeUnit.MILLISECONDS.toSeconds(MAX_INTERVAL_MS);
        }

        Instant earliest = expiries.stream().min(Instant::compareTo).orElseThrow();
        long remainingMs = earliest.toEpochMilli() - System.currentTimeMillis();
        long nextDelayMs = remainingMs - BUFFER_MS;

        return TimeUnit.MILLISECONDS.toSeconds(
                Math.clamp(nextDelayMs, MIN_INTERVAL_MS, MAX_INTERVAL_MS));
    }

    void scheduleNext(long delaySeconds) {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = scheduler.schedule(this::executeWarframeStatus, delaySeconds, TimeUnit.SECONDS);
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredNotificationHistory() {
        try {
            int retentionHours = environment.getProperty(
                    "warframe.notification.history.retention-hours",
                    Integer.class,
                    12
            );

            Instant expiredBefore = Instant.now().minus(Duration.ofHours(retentionHours));
            long deletedCount = historyRepository.deleteByNotifiedAtBefore(expiredBefore);

            log.info("定时清理通知历史记录完成 [删除数量:{}] [过期时间:{}] [保留时长:{}小时]",
                    deletedCount, expiredBefore, retentionHours);

        } catch (Exception e) {
            log.error("定时清理通知历史记录失败", e);
        }
    }
}
