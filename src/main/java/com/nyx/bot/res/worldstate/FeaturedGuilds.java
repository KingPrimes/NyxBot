package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 精选氏族
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FeaturedGuilds extends BastWorldState {
    @JsonProperty("AllianceId")
    private Id allianceId;
    @JsonProperty("Emblem")
    private Boolean emblem;
    @JsonProperty("HiddenPlatforms")
    private HiddenPlatforms hiddenPlatforms;
    @JsonProperty("IconOverride")
    private Integer iconOverride;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Tier")
    private Integer tier;

    @Data
    public static class HiddenPlatforms {
        @JsonProperty("PLATFORM_IOS")
        private Boolean ios;
    }
}
