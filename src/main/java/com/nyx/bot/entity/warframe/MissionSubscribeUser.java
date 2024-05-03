package com.nyx.bot.entity.warframe;

import com.nyx.bot.enums.SubscribeEnums;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)

@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId"}))
public class MissionSubscribeUser {


    Long subGroup;

    @Id
    Long userId;

    String userName;

    //订阅类型
    //订阅类型枚举
    SubscribeEnums subscribe;

    @Transient
    String subscribeType;

    public static class OperationMethod {


    }

}
