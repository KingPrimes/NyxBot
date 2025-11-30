package com.nyx.bot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.application.NotificationApplicationService;
import com.nyx.bot.modules.warframe.repo.NotificationHistoryRepository;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.WorldState;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class TaskWarframeStatus {

    @Resource
    ObjectMapper objectMapper;

    @Resource
    NotificationApplicationService notificationService;

    @Resource
    NotificationHistoryRepository historyRepository;

    @Resource
    Environment environment;

    @Value("${test.isTest}")
    Boolean test;

    @Async("taskExecutor")
    @Scheduled(cron = "0 0/2 * * * ?")
    public void executeWarframeStatus() {
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
                // 使用新的通知服务处理状态更新
                notificationService.handleStateUpdate(oldState, newState);

                // 更新缓存
                WarframeCache.setWarframeStatus(newState);

                log.info("Warframe 状态数据更新成功");
            } catch (Exception e) {
                log.error("Warframe 状态数据解析错误: {}", e.getMessage());
            }
        } else {
            log.error("Warframe 状态数据错误Code: {} - Body:{}", body.code(), body.body());
        }
    }

    /**
     * 定时清理所有订阅类型的过期通知历史记录
     * <p>
     * 执行时间：每天凌晨 3 点
     * 清理规则：删除 notifiedAt 早于 (当前时间 - 保留时长) 的所有记录
     * 作为兜底机制，确保即使检测器层面清理失败，也能定期清理过期数据
     * </p>
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredNotificationHistory() {
        try {
            // 从配置读取保留时长，默认 12 小时
            int retentionHours = environment.getProperty(
                    "warframe.notification.history.retention-hours",
                    Integer.class,
                    12
            );

            // 计算过期时间点
            Instant expiredBefore = Instant.now().minus(Duration.ofHours(retentionHours));

            // 执行全局清理
            long deletedCount = historyRepository.deleteByNotifiedAtBefore(expiredBefore);

            log.info("定时清理通知历史记录完成 [删除数量:{}] [过期时间:{}] [保留时长:{}小时]",
                    deletedCount, expiredBefore, retentionHours);

        } catch (Exception e) {
            log.error("定时清理通知历史记录失败", e);
            // 清理失败不应影响其他定时任务
        }
    }

    /**
     * 定时更新数据
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 0 0 1/3 * ? ")
    public void executeDataSourcePullRandom() {
        if (Math.random() < 0.5 && !test) {
            WarframeDataSource.init();
        }
    }

}
