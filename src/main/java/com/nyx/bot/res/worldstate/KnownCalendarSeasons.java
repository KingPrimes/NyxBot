package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class KnownCalendarSeasons extends BastWorldState {

    @JsonProperty("Days")
    private List<Days> days;
    @JsonProperty("Season")
    private String season;
    @JsonProperty("YearIteration")
    private Integer yearIteration;
    @JsonProperty("Version")
    private Integer version;
    @JsonProperty("UpgradeAvaliabilityRequirements")
    private List<String> upgradeAvaliabilityRequirements;

    public enum DaysTypeEnum {
        // 任务
        CET_CHALLENGE,
        // 奖励
        CET_REWARD,
        // 加成
        CET_UPGRADE,

    }

    @Data
    public static class Days {
        @JsonProperty("Day")
        private Integer day;
        @JsonProperty("Events")
        private List<Events> events;
    }

    @Data
    public static class Events {
        @JsonProperty("type")
        private DaysTypeEnum type;
        @JsonProperty("reward")
        private String reward;
        @JsonProperty("challenge")
        private String challenge;
        @JsonProperty("upgrade")
        private String upgrade;
    }
}
