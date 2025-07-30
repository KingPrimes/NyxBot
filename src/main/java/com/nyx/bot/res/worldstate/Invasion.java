package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.enums.FactionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 入侵
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Invasion extends BastWorldState {
    @JsonProperty("Faction")
    private FactionEnum faction;
    @JsonProperty("DefenderFaction")
    private FactionEnum defenderFaction;
    @JsonProperty("Node")
    private String node;
    @JsonProperty("Count")
    private Double count;
    @JsonProperty("Goal")
    private Double goal;
    @JsonProperty("LocTag")
    private String locTag;
    @JsonProperty("Completed")
    private Boolean completed;
    @JsonProperty("ChainID")
    private Id chainID;
    @JsonProperty("AttackerReward")
    private List<Reward> attackerReward;
    @JsonProperty("AttackerMissionInfo")
    private MissionInfo attackerMissionInfo;
    @JsonProperty("DefenderReward")
    private Reward defenderReward;
    @JsonProperty("DefenderMissionInfo")
    private MissionInfo defenderMissionInfo;

    @JsonIgnore
    public String getFaction() {
        return faction.getName();
    }
    @JsonIgnore
    public String getDefenderFaction() {
        return defenderFaction.getName();
    }
    @JsonIgnore
    public String getCompletion() {
        return String.format("%.2f", count / goal);
    }
}
