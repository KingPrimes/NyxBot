package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.res.enums.MissionTypeEnum;
import lombok.Data;

@Data
public class Mission {
    @JsonProperty("missionType")
    private MissionTypeEnum missionType;
    @JsonProperty("node")
    private String node;

    @JsonIgnore
    public String getMissionType(){
        return missionType.getName();
    }
}
