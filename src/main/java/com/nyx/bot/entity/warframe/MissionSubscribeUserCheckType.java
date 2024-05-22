package com.nyx.bot.entity.warframe;

import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
    Long typeId;

    Long userId;

    //订阅类型
    //订阅类型枚举
    SubscribeEnums subscribe;

    //任务类型
    WarframeMissionTypeEnum missionTypeEnum;

    //遗物纪元
    Long tierNum;

    @Transient
    String subscribeType;


}
