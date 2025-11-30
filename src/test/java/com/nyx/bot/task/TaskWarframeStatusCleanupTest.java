package com.nyx.bot.task;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.entity.NotificationHistory;
import com.nyx.bot.modules.warframe.repo.NotificationHistoryRepository;
import io.github.kingprimes.model.enums.SubscribeEnums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Warframe 状态定时任务清理功能测试
 * 测试定时清理所有订阅类型的过期历史记录
 *
 * @author Nyx Bot
 */
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Transactional
@TestPropertySource(properties = {
        "warframe.notification.history.retention-hours=12",
        "test.isTest=true"
})
class TaskWarframeStatusCleanupTest {

    @Autowired
    private NotificationHistoryRepository historyRepository;

    @Autowired
    private TaskWarframeStatus task;

    @BeforeEach
    void setUp() {
        // 清空测试数据
        historyRepository.deleteAll();
    }

    @Test
    @DisplayName("测试定时清理所有订阅类型的过期记录")
    void testCleanExpiredNotificationHistory_AllTypes() {
        // 准备测试数据：多个订阅类型的记录
        // CETUS_CYCLE：1条过期 + 1条新记录
        NotificationHistory cetuExpired = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(25, ChronoUnit.HOURS)
        );
        NotificationHistory cetuFresh = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        // VOID：1条过期 + 1条新记录
        NotificationHistory voidExpired = createHistory(
                SubscribeEnums.VOID,
                Instant.now().minus(30, ChronoUnit.HOURS)
        );
        NotificationHistory voidFresh = createHistory(
                SubscribeEnums.VOID,
                Instant.now().minus(2, ChronoUnit.HOURS)
        );

        // DAILY_DEALS：1条过期记录
        NotificationHistory dailyExpired = createHistory(
                SubscribeEnums.DAILY_DEALS,
                Instant.now().minus(26, ChronoUnit.HOURS)
        );

        historyRepository.save(cetuExpired);
        historyRepository.save(cetuFresh);
        historyRepository.save(voidExpired);
        historyRepository.save(voidFresh);
        historyRepository.save(dailyExpired);

        // 验证初始状态：5条记录
        assertEquals(5, historyRepository.count());

        // 执行定时清理
        task.cleanExpiredNotificationHistory();

        // 验证结果：应删除3条过期记录，保留2条新记录
        assertEquals(2, historyRepository.count());

        // 验证过期记录被删除
        assertFalse(historyRepository.existsById(cetuExpired.getId()));
        assertFalse(historyRepository.existsById(voidExpired.getId()));
        assertFalse(historyRepository.existsById(dailyExpired.getId()));

        // 验证新记录保持不变
        assertTrue(historyRepository.existsById(cetuFresh.getId()));
        assertTrue(historyRepository.existsById(voidFresh.getId()));
    }

    @Test
    @DisplayName("测试定时清理空数据库不报错")
    void testCleanExpiredNotificationHistory_EmptyDatabase() {
        // 验证数据库为空
        assertEquals(0, historyRepository.count());

        // 执行清理，应不抛出异常
        assertDoesNotThrow(() -> task.cleanExpiredNotificationHistory());

        // 验证数据库仍为空
        assertEquals(0, historyRepository.count());
    }

    @Test
    @DisplayName("测试定时清理大量过期数据")
    void testCleanExpiredNotificationHistory_LargeDataset() {
        // 准备测试数据：100条过期记录
        for (int i = 0; i < 100; i++) {
            NotificationHistory expired = createHistory(
                    i % 2 == 0 ? SubscribeEnums.CETUS_CYCLE : SubscribeEnums.VOID,
                    Instant.now().minus(25 + i, ChronoUnit.HOURS)
            );
            historyRepository.save(expired);
        }

        // 准备测试数据：10条新记录
        for (int i = 0; i < 10; i++) {
            NotificationHistory fresh = createHistory(
                    i % 2 == 0 ? SubscribeEnums.CETUS_CYCLE : SubscribeEnums.VOID,
                    Instant.now().minus(i, ChronoUnit.HOURS)
            );
            historyRepository.save(fresh);
        }

        // 验证初始状态：110条记录
        assertEquals(110, historyRepository.count());

        // 执行定时清理
        long startTime = System.currentTimeMillis();
        task.cleanExpiredNotificationHistory();
        long endTime = System.currentTimeMillis();

        // 验证结果：应删除100条过期记录，保留10条新记录
        assertEquals(10, historyRepository.count());

        // 验证性能：清理100条记录应在1秒内完成
        long duration = endTime - startTime;
        assertTrue(duration < 1000, "清理耗时过长: " + duration + "ms");
    }

    @Test
    @DisplayName("测试定时清理只删除过期记录")
    void testCleanExpiredNotificationHistory_OnlyExpired() {
        // 准备测试数据：各种时间点的记录
        NotificationHistory veryOld = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(72, ChronoUnit.HOURS) // 3天前
        );
        NotificationHistory old = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(25, ChronoUnit.HOURS) // 25小时前
        );
        NotificationHistory nearBoundary = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(24, ChronoUnit.HOURS) // 恰好24小时
        );
        NotificationHistory justWithin = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(23, ChronoUnit.HOURS) // 23小时前
        );
        NotificationHistory fresh = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(1, ChronoUnit.HOURS) // 1小时前
        );

        historyRepository.save(veryOld);
        historyRepository.save(old);
        historyRepository.save(nearBoundary);
        historyRepository.save(justWithin);
        historyRepository.save(fresh);

        // 验证初始状态：5条记录
        assertEquals(5, historyRepository.count());

        // 执行定时清理
        task.cleanExpiredNotificationHistory();

        // 验证结果：应删除3条超过24小时的记录
        assertEquals(2, historyRepository.count());

        // 验证过期记录被删除
        assertFalse(historyRepository.existsById(veryOld.getId()));
        assertFalse(historyRepository.existsById(old.getId()));
        assertFalse(historyRepository.existsById(nearBoundary.getId()));

        // 验证有效记录保持不变
        assertTrue(historyRepository.existsById(justWithin.getId()));
        assertTrue(historyRepository.existsById(fresh.getId()));
    }

    /**
     * 创建测试用的通知历史记录
     */
    private NotificationHistory createHistory(SubscribeEnums type, Instant notifiedAt) {
        NotificationHistory history = new NotificationHistory();
        history.setSubscribeType(type);
        history.setExpiryTimestamp(notifiedAt.getEpochSecond() + 1000);
        history.setNotifiedAt(notifiedAt);
        history.setCycleState("test");
        return history;
    }
}