package com.nyx.bot.entity.warframe;

import com.nyx.bot.enums.SubscribeEnums;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
public class MissionSubscribeGroupCheckType {
    @Id
    Long typeId;

    Long subGroup;

    //订阅类型
    //订阅类型枚举
    SubscribeEnums subscribe;
    // 是否开启
    Boolean checkType;
}
