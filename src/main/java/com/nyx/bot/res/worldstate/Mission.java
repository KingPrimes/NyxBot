package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.enums.MissionTypeEnum;
import lombok.Data;

@Data
public class Mission {
    @JsonProperty("missionType")
    private MissionTypeEnum missionType;
    @JsonProperty("node")
    private String node;
}
