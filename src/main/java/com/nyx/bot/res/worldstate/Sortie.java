package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 突击
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class Sortie extends BastWorldState{

    @JsonProperty("Boss")
    private String boss;
    @JsonProperty("Reward")
    private String reward;
    @JsonProperty("Seed")
    private Integer seed;
    @JsonProperty("Variants")
    private List<Variant> variants;
    @JsonProperty("Twitter")
    private Boolean twitter;
}
