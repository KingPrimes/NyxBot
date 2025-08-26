package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.res.enums.FactionEnum;
import lombok.Data;

import java.util.List;

/**
 *
 */

@Data
public class MissionInfo {
    @JsonProperty("seed")
    private Integer seed;
    @JsonProperty("faction")
    private FactionEnum faction;
    @JsonProperty("missionReward")
    private List<Reward> missionReward; // 添加这个字段

    @JsonIgnore
    public String getFaction() {
        return faction.getName();
    }
}
