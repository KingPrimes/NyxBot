package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.entity.warframe.exprot.Nodes;
import com.nyx.bot.res.enums.MissionTypeEnum;
import com.nyx.bot.res.enums.VoidEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 裂隙任务
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ActiveMission extends BastWorldState {
    // 任务类型
    @JsonProperty("MissionType")
    private MissionTypeEnum missionType;
    // 遗物等级
    @JsonProperty("Modifier")
    private VoidEnum modifier;
    // 节点
    @JsonProperty("Node")
    private String node;
    @JsonIgnore
    private Nodes nodes;
    //
    @JsonProperty("Region")
    private Integer region;
    // 种子
    @JsonProperty("Seed")
    private Integer seed;
    // 是否是钢铁模式
    @JsonProperty("Hard")
    private Boolean hard;

    @JsonIgnore
    public String getMissionType() {
        return missionType.getName();
    }

    @JsonIgnore
    public String getModifier() {
        return modifier.getName();
    }

    @JsonIgnore
    public VoidEnum getVoidEnum() {
        return modifier;
    }
}
