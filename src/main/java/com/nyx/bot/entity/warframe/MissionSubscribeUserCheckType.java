package com.nyx.bot.entity.warframe;

import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
public class MissionSubscribeUserCheckType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long typeId;

    //订阅类型
    //订阅类型枚举
    SubscribeEnums subscribe;

    //任务类型
    WarframeMissionTypeEnum missionTypeEnum;

    //遗物纪元
    Integer tierNum;

    @Transient
    String subscribeType;


}
