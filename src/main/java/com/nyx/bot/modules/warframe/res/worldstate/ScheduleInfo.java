package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduleInfo extends BastWorldState{

    // 物品组合包
    @JsonProperty("FeaturedItem")
    String item;

}
