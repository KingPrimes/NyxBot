package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nyx.bot.common.core.dao.BaseEntity;
import com.nyx.bot.modules.warframe.enums.InvasionReward;
import com.nyx.bot.modules.warframe.enums.MissionType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(
        name = "mission_subscribe_user_check_type",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_subscribe_rule",
                        columnNames = {"subu_id", "subscribe", "mission_type_enum", "tier_num"}
                )
        }
)
public class MissionSubscribeUserCheckType extends BaseEntity {

    //订阅类型
    //订阅类型枚举
    @Enumerated(EnumType.STRING)
    @Column(name = "subscribe", nullable = false, length = 50)
    SubscribeType subscribe;

    //任务类型
    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type_enum", length = 50)
    MissionType missionTypeEnum;

    //遗物纪元
    @Column(name = "tier_num")
    Integer tierNum;

    //入侵奖励物品（仅 INVISIONS 类型使用，null 表示全部）
    @Enumerated(EnumType.STRING)
    @Column(name = "invasion_reward", length = 50)
    InvasionReward invasionReward;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subu_id", nullable = false)
    @JsonIgnore
    private MissionSubscribeUser subscribeUser;

    @JsonIgnore
    @Transient
    public boolean matches(SubscribeType type,
                           MissionType missionType,
                           Integer tier,
                           InvasionReward reward) {
        return this.subscribe == type &&
                (missionType == null || this.missionTypeEnum == missionType) &&
                (tier == null || Objects.equals(this.tierNum, tier)) &&
                (reward == null || this.invasionReward == reward);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionSubscribeUserCheckType that = (MissionSubscribeUserCheckType) o;
        return subscribe == that.subscribe && missionTypeEnum == that.missionTypeEnum && Objects.equals(tierNum, that.tierNum) && invasionReward == that.invasionReward && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscribe, missionTypeEnum, tierNum, invasionReward, id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("subscribe", subscribe)
                .append("missionTypeEnum", missionTypeEnum)
                .append("tierNum", tierNum)
                .append("invasionReward", invasionReward)
                .append("id", id)
                .toString();
    }
}
