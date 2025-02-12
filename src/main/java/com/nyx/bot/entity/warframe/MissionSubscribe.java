package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 订阅
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"subGroup"}))
@JsonView(Views.View.class)
public class MissionSubscribe extends BaseEntity {

    //订阅的群组
    @Id
    Long subGroup;

    String groupName;

    //订阅的用户
    // fetch 加载方式
    // targetEntity 关联的目标实体类
    // cascade 联级操作，增删改
    @OneToMany(
            fetch = FetchType.EAGER,
            targetEntity = MissionSubscribeUser.class,
            cascade = CascadeType.ALL
    )
    // name = 外键名称
    // referencedColumnName = 被关联的键名称
    // nullable = false 外键列不可为 null。
    @JoinColumn(
            name = "groupUid",
            referencedColumnName = "subGroup",
            nullable = false
    )
    List<MissionSubscribeUser> subUsers = new ArrayList<>();


    @OneToMany(
            fetch = FetchType.EAGER,
            targetEntity = MissionSubscribeGroupCheckType.class,
            cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "subGroupCheckType",
            referencedColumnName = "subGroup",
            nullable = false
    )
    List<MissionSubscribeGroupCheckType> checkTypes = new ArrayList<>();

    //发送消息的Bot
    Long subBotUid;

}
