package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class VoidTrader extends BastWorldState {

    @JsonProperty("Character")
    private String character;
    @JsonProperty("Node")
    private String node;

    @JsonProperty("Manifest")
    private List<Manifest> manifest;

    @Data
    public static class Manifest {
        // 物品名称
        @JsonProperty("ItemType")
        private String item;
        // 杜卡币
        @JsonProperty("PrimePrice")
        private Integer primePrice;
        // 星币
        @JsonProperty("RegularPrice")
        private Long regularPrice;
        // 限购
        @JsonProperty("Limit")
        private Integer limit;
    }
}
