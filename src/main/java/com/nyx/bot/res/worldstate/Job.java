package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.entity.warframe.exprot.reward.RewardPool;
import lombok.Data;

import java.util.List;

@Data
public class Job {
    /**
     * 任务类型
     */
    @JsonProperty("jobType")
    private String type;
    /**
     * 任务描述
     */
    @JsonProperty("desc")
    private String desc;
    /**
     * 任务奖励
     */
    @JsonProperty("rewards")
    private String rewards;

    @JsonProperty("rewardPool")
    private RewardPool rewardPool;
    /**
     * 段位限制
     */
    @JsonProperty("masteryReq")
    private Integer masteryReq;
    /**
     * 敌人 最小等级
     */
    @JsonProperty("minEnemyLevel")
    private Integer minLevel;
    /**
     * 敌人 最大等级
     */
    @JsonProperty("maxEnemyLevel")
    private Integer maxLevel;
    /**
     * 任务奖励 XP
     */
    @JsonProperty("xpAmounts")
    private List<Integer> xpAmounts;
    /**
     * 是否为无尽任务
     */
    @JsonProperty("endless")
    private Boolean endless;

    @JsonProperty("locationTag")
    private String locationTag;
    /**
     * 是否为保险库任务
     */
    @JsonProperty("isVault")
    private Boolean isVault;
}
