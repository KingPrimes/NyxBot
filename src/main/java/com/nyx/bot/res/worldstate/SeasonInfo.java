package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.entity.warframe.exprot.Nightwave;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 电波
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class SeasonInfo extends BastWorldState {

    @JsonProperty("AffiliationTag")
    private String affiliationTag;

    @JsonProperty("Season")
    private Integer season;

    @JsonProperty("Phase")
    private Integer phase;

    @JsonProperty("Params")
    private String params;

    @JsonProperty("ActiveChallenges")
    private List<ActiveChallenges> activeChallenges;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ActiveChallenges extends BastWorldState {
        @JsonProperty("Daily")
        private Boolean daily;

        @JsonProperty("Challenge")
        private String challenge;

        Nightwave nightwave;
    }

}
