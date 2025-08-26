package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.res.enums.BossEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 突击
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class Sortie extends BastWorldState {

    @JsonProperty("Boss")
    private BossEnum boss;
    @JsonProperty("Reward")
    private String reward;
    @JsonProperty("Seed")
    private Integer seed;
    @JsonProperty("Variants")
    private List<Variant> variants;
    @JsonProperty("Twitter")
    private Boolean twitter;

    @JsonIgnore
    public String getBoss() {
        return boss.getName();
    }
}
