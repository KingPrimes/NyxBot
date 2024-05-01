package com.nyx.bot.entity.warframe;

import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.SubscribeEnums;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId"}))
public class MissionSubscribeUsers extends BaseEntity {


    Long subGroup;

    String userName;

    @Id
    Long userId;

    //订阅类型
    SubscribeEnums subscribe;

    public static class OperationMethod {

    }

}
