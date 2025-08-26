package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.res.enums.MissionTypeEnum;
import com.nyx.bot.modules.warframe.res.enums.ModifierTypeEnum;
import lombok.Data;

@Data
public class Variant {
    // 任务类型
    @JsonProperty("missionType")
    private MissionTypeEnum missionType;
    // 状态类型
    @JsonProperty("modifierType")
    private ModifierTypeEnum modifierType;
    // 节点
    @JsonProperty("node")
    private String node;
    // 地图板块
    @JsonProperty("tileset")
    private String tileset;

    @JsonIgnore
    public String getMissionType() {
        return missionType.getName();
    }

    @JsonIgnore
    public String getModifierType() {
        return modifierType.getStr();
    }
}
