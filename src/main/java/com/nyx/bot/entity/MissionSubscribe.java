package com.nyx.bot.entity;

import com.nyx.bot.enums.SubscribeEnums;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 订阅
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"subGroup","subUser"}))
public class MissionSubscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    //订阅的群组
    Long subGroup;
    //订阅的用户
    Long subUser;
    //发送消息的Bot
    Long subBotUid;
    //订阅类型
    SubscribeEnums subscribe;

}
