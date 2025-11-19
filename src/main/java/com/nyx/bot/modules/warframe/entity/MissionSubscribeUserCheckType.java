package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nyx.bot.common.core.dao.BaseEntity;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table
public class MissionSubscribeUserCheckType extends BaseEntity {

    //订阅类型
    //订阅类型枚举
    @Column(nullable = false)
    SubscribeEnums subscribe;

    //任务类型
    @Column
    MissionTypeEnum missionTypeEnum;

    //遗物纪元
    @Column
    Integer tierNum;

    @Transient
    String subscribeType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subu_id")
    @JsonIgnore
    private MissionSubscribeUser subscribeUser;

    @JsonIgnore
    @Transient
    public boolean matches(SubscribeEnums type,
                           MissionTypeEnum missionType,
                           Integer tier) {
        return this.subscribe == type &&
                (missionType == null || this.missionTypeEnum == missionType) &&
                (tier == null || Objects.equals(this.tierNum, tier));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionSubscribeUserCheckType that = (MissionSubscribeUserCheckType) o;
        return subscribe == that.subscribe && missionTypeEnum == that.missionTypeEnum && Objects.equals(tierNum, that.tierNum) && Objects.equals(subscribeType, that.subscribeType) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscribe, missionTypeEnum, tierNum, subscribeType, id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("subscribe", subscribe)
                .append("missionTypeEnum", missionTypeEnum)
                .append("tierNum", tierNum)
                .append("subscribeType", subscribeType)
                .append("id", id)
                .toString();
    }
}
