package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.entity.exprot.Nodes;
import com.nyx.bot.modules.warframe.res.enums.MissionTypeEnum;
import com.nyx.bot.modules.warframe.res.enums.VoidEnum;
import io.github.kingprimes.model.enums.FactionEnum;
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

    private FactionEnum faction;
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
        if (missionType == null) return MissionTypeEnum.MT_DEFAULT.getName();
        return missionType.getName();
    }

    @JsonIgnore
    public String getModifier() {
        if (modifier == null) return VoidEnum.VoidT1.getName();
        return modifier.getName();
    }

    @JsonIgnore
    public VoidEnum getVoidEnum() {
        return modifier;
    }
}
