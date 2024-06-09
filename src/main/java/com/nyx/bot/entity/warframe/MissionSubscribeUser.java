package com.nyx.bot.entity.warframe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table
public class MissionSubscribeUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long msUid;

    Long userId;

    String userName;

    //订阅的用户
    // fetch 加载方式
    // targetEntity 关联的目标实体类
    // cascade 联级操作，增删改
    @OneToMany(
            fetch = FetchType.EAGER,
            targetEntity = MissionSubscribeUserCheckType.class,
            cascade = CascadeType.ALL
    )
    // name = 外键名称
    // referencedColumnName = 被关联的键名称
    // nullable = false 外键列不可为 null。
    @JoinColumn(
            name = "subUserCheckType",
            referencedColumnName = "userId",
            nullable = false
    )
    List<MissionSubscribeUserCheckType> typeList = new ArrayList<>();
}
