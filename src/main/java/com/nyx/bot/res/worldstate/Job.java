package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Job {
    @JsonProperty("jobType")
    private String type;
    @JsonProperty("rewards")
    private String rewards;
    @JsonProperty("masteryReq")
    private Integer masteryReq;
    @JsonProperty("minEnemyLevel")
    private Integer minLevel;
    @JsonProperty("maxEnemyLevel")
    private Integer maxLevel;
    @JsonProperty("xpAmounts")
    private List<Integer> xpAmounts;
    /**
     * 是否为无尽任务
     */
    @JsonProperty("endless")
    private Boolean endless;
    @JsonProperty("locationTag")
    private String locationTag;
    @JsonProperty("isVault")
    private Boolean isVault;
}
