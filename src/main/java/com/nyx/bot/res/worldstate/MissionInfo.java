package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.enums.FactionEnum;
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

    public String getFaction() {
        return faction.getName();
    }
}
