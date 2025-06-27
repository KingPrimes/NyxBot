package com.nyx.bot.res.worldstate;

import lombok.Data;

import java.util.List;

@Data
public class Job {
    private String jobType;
    private String rewards;
    private Integer masteryReq;
    private Integer minEnemyLevel;
    private Integer maxEnemyLevel;
    private List<Integer> xpAmounts;
    private Boolean endless;
    private String locationTag;
    private Boolean isVault;
}
