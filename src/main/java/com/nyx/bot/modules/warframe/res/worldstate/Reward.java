package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Reward {
    @JsonProperty("credits")
    private Integer credits;
    @JsonProperty("xp")
    private Integer xp;
    @JsonProperty("items")
    private List<String> items;
    @JsonProperty("countedItems")
    private List<Item> countedItems;

    @Data
    public static class Item {
        @JsonProperty("ItemType")
        private String name;
        @JsonProperty("ItemCount")
        private Integer count;
    }
}
