package com.nyx.bot.modules.warframe.infrastructure.detector;

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
 * 夜灵平原周期检测器测试
 * 主要测试清理过期通知历史记录的功能
 */
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Transactional
@TestPropertySource(properties = {
        "warframe.notification.history.retention-hours=12"
})
class CetusCycleChangeDetectorTest {

    @Autowired
    private NotificationHistoryRepository historyRepository;

    @Autowired
    private CetusCycleChangeDetector detector;

    @BeforeEach
    void setUp() {
        // 清空测试数据
        historyRepository.deleteAll();
    }

    @Test
    @DisplayName("测试清理超过24小时的过期记录")
    void testCleanExpiredHistory_OldRecords() {
        // 准备测试数据：3条记录
        // 1. 过期超过24小时的记录
        NotificationHistory expired = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(25, ChronoUnit.HOURS),
                "day"
        );

        // 2. 过期12小时的记录（应保留）
        NotificationHistory halfExpired = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(12, ChronoUnit.HOURS),
                "day"
        );

        // 3. 新记录（1小时前，应保留）
        NotificationHistory fresh = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(1, ChronoUnit.HOURS),
                "night"
        );

        historyRepository.save(expired);
        historyRepository.save(halfExpired);
        historyRepository.save(fresh);

        // 验证初始状态：3条记录
        assertEquals(3, historyRepository.count());

        // 执行清理
        detector.cleanExpiredHistory();

        // 验证结果：应删除1条过期记录，保留2条
        assertEquals(2, historyRepository.count());

        // 验证保留的记录是正确的
        assertFalse(historyRepository.existsById(expired.getId()));
        assertTrue(historyRepository.existsById(halfExpired.getId()));
        assertTrue(historyRepository.existsById(fresh.getId()));
    }

    @Test
    @DisplayName("测试不同订阅类型的记录隔离")
    void testCleanExpiredHistory_DifferentTypes() {
        // 准备测试数据：不同订阅类型的过期记录
        // CETUS_CYCLE 类型：2条过期记录
        NotificationHistory cetuExpired1 = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(25, ChronoUnit.HOURS),
                "day"
        );
        NotificationHistory cetuExpired2 = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(30, ChronoUnit.HOURS),
                "night"
        );

        // VOID 类型：2条过期记录（应保留，因为清理方法只清理CETUS_CYCLE）
        NotificationHistory voidExpired1 = createHistory(
                SubscribeEnums.VOID,
                Instant.now().minus(25, ChronoUnit.HOURS),
                null
        );
        NotificationHistory voidExpired2 = createHistory(
                SubscribeEnums.VOID,
                Instant.now().minus(30, ChronoUnit.HOURS),
                null
        );

        historyRepository.save(cetuExpired1);
        historyRepository.save(cetuExpired2);
        historyRepository.save(voidExpired1);
        historyRepository.save(voidExpired2);

        // 验证初始状态：4条记录
        assertEquals(4, historyRepository.count());

        // 执行清理（只清理CETUS_CYCLE类型）
        detector.cleanExpiredHistory();

        // 验证结果：应删除2条CETUS_CYCLE记录，保留2条VOID记录
        assertEquals(2, historyRepository.count());

        // 验证CETUS_CYCLE记录被删除
        assertFalse(historyRepository.existsById(cetuExpired1.getId()));
        assertFalse(historyRepository.existsById(cetuExpired2.getId()));

        // 验证VOID记录保持不变
        assertTrue(historyRepository.existsById(voidExpired1.getId()));
        assertTrue(historyRepository.existsById(voidExpired2.getId()));
    }

    @Test
    @DisplayName("测试空数据库不报错")
    void testCleanExpiredHistory_EmptyDatabase() {
        // 验证数据库为空
        assertEquals(0, historyRepository.count());

        // 执行清理，应不抛出异常
        assertDoesNotThrow(() -> detector.cleanExpiredHistory());

        // 验证数据库仍为空
        assertEquals(0, historyRepository.count());
    }

    @Test
    @DisplayName("测试所有记录都是新记录时不删除")
    void testCleanExpiredHistory_AllFreshRecords() {
        // 准备测试数据：3条新记录（都在24小时内）
        NotificationHistory fresh1 = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(1, ChronoUnit.HOURS),
                "day"
        );
        NotificationHistory fresh2 = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(5, ChronoUnit.HOURS),
                "night"
        );
        NotificationHistory fresh3 = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(23, ChronoUnit.HOURS),
                "day"
        );

        historyRepository.save(fresh1);
        historyRepository.save(fresh2);
        historyRepository.save(fresh3);

        // 验证初始状态：3条记录
        assertEquals(3, historyRepository.count());

        // 执行清理
        detector.cleanExpiredHistory();

        // 验证结果：所有记录都应保留
        assertEquals(3, historyRepository.count());
        assertTrue(historyRepository.existsById(fresh1.getId()));
        assertTrue(historyRepository.existsById(fresh2.getId()));
        assertTrue(historyRepository.existsById(fresh3.getId()));
    }

    @Test
    @DisplayName("测试边界情况：恰好12小时的记录")
    void testCleanExpiredHistory_BoundaryCase() {
        // 准备测试数据：恰好12小时的记录
        NotificationHistory boundary = createHistory(
                SubscribeEnums.CETUS_CYCLE,
                Instant.now().minus(12, ChronoUnit.HOURS),
                "day"
        );

        historyRepository.save(boundary);

        // 验证初始状态：1条记录
        assertEquals(1, historyRepository.count());

        // 执行清理
        detector.cleanExpiredHistory();

        // 验证结果：边界情况的记录应该被删除
        // 因为清理逻辑使用 "早于" (<) 而不是 "早于等于" (<=)
        assertEquals(0, historyRepository.count());
    }

    /**
     * 创建测试用的通知历史记录
     */
    private NotificationHistory createHistory(SubscribeEnums type, Instant notifiedAt, String cycleState) {
        NotificationHistory history = new NotificationHistory();
        history.setSubscribeType(type);
        history.setExpiryTimestamp(notifiedAt.getEpochSecond() + 1000);
        history.setNotifiedAt(notifiedAt);
        history.setCycleState(cycleState);
        return history;
    }
}