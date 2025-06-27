package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 每日特惠
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DailyDeals extends BastWorldState {

    // 剩余数量
    @JsonProperty("AmountSold")
    private Integer sold;
    // 总数
    @JsonProperty("AmountTotal")
    private Integer total;
    // 折扣
    @JsonProperty("Discount")
    private Integer count;
    // 原价
    @JsonProperty("OriginalPrice")
    private Integer originalPrice;
    // 售价
    @JsonProperty("SalePrice")
    private Integer salePrice;
    // 商品
    @JsonProperty("StoreItem")
    private String item;

}
