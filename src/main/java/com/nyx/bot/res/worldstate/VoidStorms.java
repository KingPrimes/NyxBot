package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.enums.VoidEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VoidStorms extends BastWorldState {
    // 节点
    @JsonProperty("Node")
    private String node;
    // 遗物等级
    @JsonProperty("ActiveMissionTier")
    private VoidEnum ActiveMissionTier;

    public String getActiveMissionTier() {
        return ActiveMissionTier.getName();
    }

    @JsonIgnore
    public VoidEnum getVoidEnum() {
        return ActiveMissionTier;
    }
}
