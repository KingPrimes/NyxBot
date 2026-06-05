package com.nyx.bot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.application.NotificationApplicationService;
import com.nyx.bot.modules.warframe.repo.NotificationHistoryRepository;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SyndicateEnum;
import io.github.kingprimes.model.worldstate.Cycle;
import io.github.kingprimes.model.worldstate.DateField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
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
    static final long BUFFER_MS = TimeUnit.SECONDS.toMillis(30);

    private final ObjectMapper objectMapper;
    private final NotificationApplicationService notificationService;
    private final NotificationHistoryRepository historyRepository;
    private final Environment environment;
    /**
     * 定时触发器（仅负责延迟后回调，不执行业务逻辑）
     */
    private final ScheduledExecutorService scheduler;
    /**
     * 有界执行器（受 Semaphore 限流保护）
     */
    private final ExecutorService boundedExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledFuture;

    public TaskWarframeStatus(ObjectMapper objectMapper,
                              NotificationApplicationService notificationService,
                              NotificationHistoryRepository historyRepository,
                              Environment environment,
                              ScheduledExecutorService scheduler,
                              ExecutorService taskExecutor) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.historyRepository = historyRepository;
        this.environment = environment;
        this.scheduler = scheduler;
        this.boundedExecutor = taskExecutor;
    }

    /**
     * 启动动态轮询调度。
     * 由 WarframeDataSource.init() 在数据初始化完成后调用。
     * scheduler 只负责延迟触发，实际任务提交到 boundedExecutor 受 Semaphore 限流。
     */
    public void startSchedule() {
        scheduler.schedule(() -> boundedExecutor.execute(this::executeWarframeStatus), 5, TimeUnit.SECONDS);
        log.info("动态轮询调度已启动，首次执行延迟 5 秒");
    }

    public void executeWarframeStatus() {
        if (!running.compareAndSet(false, true)) {
            log.debug("上一次轮询尚未完成，跳过本次");
            return;
        }
        try {
            executeWithRetry();
        } finally {
            running.set(false);
        }
    }

    private void executeWithRetry() {
        int maxRetries = 3;
        long retryDelaySeconds = 10;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            boolean success = false;
            try {
                HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
                if (body.is2xxSuccessful() || body.is3xxRedirection()) {
                    try {
                        String rawJson = body.body();
                        WorldState newState = objectMapper.readValue(rawJson, WorldState.class);
                        WorldState oldState;
                        try {
                            oldState = WarframeCache.getWarframeStatus();
                        } catch (DataNotInfoException e) {
                            oldState = newState;
                        }
                        notificationService.handleStateUpdate(oldState, newState);

                        List<Instant> expiries = collectExpiryTimestamps(newState);
//                        if (log.isDebugEnabled()) {
//                            var fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
//                            log.debug("{}", expiries.stream().map(fmt::format).toList());
//                        }
                        long nextDelaySeconds = calculateNextDelay(expiries);
                        long ttlSeconds = nextDelaySeconds + TimeUnit.MILLISECONDS.toSeconds(BUFFER_MS);

                        WarframeCache.setWarframeStatus(newState, rawJson, ttlSeconds);

                        log.debug("Warframe 状态数据更新成功，下次轮询: {}s 后", nextDelaySeconds);
                        scheduleNext(nextDelaySeconds);
                        success = true;
                    } catch (Exception e) {
                        log.error("Warframe 状态数据解析错误 (第 {} 次尝试): {}", attempt, e.getMessage());
                    }
                } else {
                    log.error("Warframe 状态数据错误Code: {} (第 {} 次尝试) - Body:{}", body.code(), attempt, body.body());
                }
            } catch (Exception e) {
                log.error("Warframe 状态数据请求异常 (第 {} 次尝试): {}", attempt, e.getMessage());
            }

            if (success) {
                return;
            }
            if (attempt < maxRetries) {
                log.warn("等待 {}s 后重试 (第 {} / {} 次)...", retryDelaySeconds, attempt + 1, maxRetries);
                try {
                    TimeUnit.SECONDS.sleep(retryDelaySeconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.error("Warframe 状态数据获取失败，已重试 {} 次，等待下次轮询", maxRetries);
        scheduleNext(TimeUnit.MILLISECONDS.toSeconds(MAX_INTERVAL_MS));
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
        //    注：CetusCycle/CambionCycle 的 expiry 由构造函数基于 now 实时计算，轮询调度不可靠，
        //    改为直接从赏金任务数据取固定时间戳。
        Stream.of(ws.getCetusCycle(), ws.getEarthCycle(), ws.getVallisCycle(),
                        ws.getZarimanCycle(), ws.getDuvalierCycle(), ws.getSteelPath())
                .filter(Objects::nonNull)
                .map(Cycle::getExpiry)
                .filter(Objects::nonNull)
                .forEach(expiries::add);

        // C. Cetus 赏金任务固定过期时间（替代 CetusCycle/CambionCycle 的计算值）
        ws.getSyndicateMissions().stream()
                .filter(s -> s.getTag() != null && s.getTag() == SyndicateEnum.CetusSyndicate)
                .findFirst()
                .map(s -> safeEpochSecond(s.getExpiry()))
                .filter(Objects::nonNull).ifPresent(expiries::add);

        Instant now = Instant.now();
        expiries.removeIf(expiry -> !expiry.isAfter(now));

        return expiries.stream()
                .map(e -> e.truncatedTo(ChronoUnit.MINUTES))
                .distinct()
                .toList();
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

    /**
     * 调度下一次轮询。
     * scheduler 轻量触发 → boundedExecutor 受 Semaphore 保护执行实际任务。
     */
    void scheduleNext(long delaySeconds) {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = scheduler.schedule(
                () -> boundedExecutor.execute(this::executeWarframeStatus),
                delaySeconds, TimeUnit.SECONDS);
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
