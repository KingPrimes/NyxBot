package com.nyx.bot.entity.warframe;

import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.SubscribeEnums;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订阅
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"subGroup", "subUser"}))
public class MissionSubscribe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    //订阅的群组
    Long subGroup;
    //订阅的用户
    String subUser;
    //发送消息的Bot
    Long subBotUid;
    //订阅类型
    SubscribeEnums subscribe;

}
