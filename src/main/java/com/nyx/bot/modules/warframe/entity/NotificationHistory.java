package com.nyx.bot.modules.warframe.entity;

import com.nyx.bot.common.core.dao.BaseEntity;
import io.github.kingprimes.model.enums.SubscribeEnums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * 通知历史记录
 * 用于防止重复通知
 * 
 * @author Nyx Bot
 */
@Getter
@Setter
@Entity
@Table(name = "notification_history", 
       indexes = {
           @Index(name = "idx_type_expiry", columnList = "subscribe_type,expiry_timestamp", unique = true)
       })
public class NotificationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订阅类型
     */
    @Column(name = "subscribe_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscribeEnums subscribeType;

    /**
     * 周期过期时间戳（秒）
     * 用于唯一标识一个周期
     */
    @Column(name = "expiry_timestamp", nullable = false)
    private Long expiryTimestamp;

    /**
     * 通知时间
     */
    @Column(name = "notified_at", nullable = false)
    private Instant notifiedAt;

    /**
     * 周期状态（可选，用于调试）
     */
    @Column(name = "cycle_state")
    private String cycleState;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationHistory that = (NotificationHistory) o;
        return Objects.equals(subscribeType, that.subscribeType) && 
               Objects.equals(expiryTimestamp, that.expiryTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscribeType, expiryTimestamp);
    }
}