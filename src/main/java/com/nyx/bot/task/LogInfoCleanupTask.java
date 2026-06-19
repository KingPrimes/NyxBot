package com.nyx.bot.task;

import com.nyx.bot.modules.system.repo.LogInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * 操作日志定时清理任务
 * <p>
 * LogInfoAspect 在每次 Web 操作 / 机器人指令执行时都会写入一条 LogInfo 记录，
 * 该表无任何保留策略，长期运行会持续膨胀（H2 文件 + 页缓存常驻内存）。
 * 本任务每天凌晨清理超过保留期的历史日志，止住无限增长。
 *
 * @author KingPrimes
 */
@Slf4j
@Component
public class LogInfoCleanupTask {

    private final LogInfoRepository logInfoRepository;
    private final Environment environment;

    public LogInfoCleanupTask(LogInfoRepository logInfoRepository, Environment environment) {
        this.logInfoRepository = logInfoRepository;
        this.environment = environment;
    }

    /**
     * 每天凌晨 3:30 清理过期操作日志，与通知历史清理（3:00）错峰执行。
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanExpiredLogInfo() {
        try {
            int retentionDays = environment.getProperty(
                    "system.log.retention-days",
                    Integer.class,
                    7
            );

            Date expiredBefore = Date.from(Instant.now().minus(Duration.ofDays(retentionDays)));
            long deletedCount = logInfoRepository.deleteByLogTimeBefore(expiredBefore);

            log.info("定时清理操作日志完成 [删除数量:{}] [过期时间:{}] [保留天数:{}天]",
                    deletedCount, expiredBefore, retentionDays);

        } catch (Exception e) {
            log.error("定时清理操作日志失败", e);
        }
    }
}
