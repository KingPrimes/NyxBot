package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.enums.BossEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class LiteSorite extends BastWorldState{
    @JsonProperty("Reward")
    private String reward;
    @JsonProperty("Seed")
    private Integer seed;
    @JsonProperty("Boss")
    private BossEnum boss;
    @JsonProperty("Missions")
    private List<Mission> missions;

    public String getBoss() {
        return boss.getName();
    }
}
