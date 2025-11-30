package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.NotificationHistory;
import io.github.kingprimes.model.enums.SubscribeEnums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * 通知历史记录仓储
 * 用于查询和保存通知历史，防止重复通知
 * 
 * @author Nyx Bot
 */
@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    /**
     * 检查指定订阅类型和过期时间戳的通知是否已存在
     *
     * @param subscribeType    订阅类型
     * @param expiryTimestamp  过期时间戳（秒）
     * @return 如果已存在返回 true
     */
    boolean existsBySubscribeTypeAndExpiryTimestamp(SubscribeEnums subscribeType, Long expiryTimestamp);

    /**
     * 查找指定订阅类型和过期时间戳的通知记录
     *
     * @param subscribeType    订阅类型
     * @param expiryTimestamp  过期时间戳（秒）
     * @return 通知历史记录
     */
    Optional<NotificationHistory> findBySubscribeTypeAndExpiryTimestamp(SubscribeEnums subscribeType, Long expiryTimestamp);

    /**
     * 删除指定时间之前的历史记录（清理过期数据）
     *
     * @param before 指定时间之前
     * @return 删除的记录数
     */
    long deleteByNotifiedAtBefore(Instant before);
}