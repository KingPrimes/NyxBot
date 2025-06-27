package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlashSale {
    private Integer bogoBuy;
    private Integer bogoGet;
    private Boolean dailySaleGenerated;
    // 折扣
    private Integer discount;
    // 结束时间
    private DateField endDate;
    // 是否隐藏
    private Boolean hideFromMarket;
    // 售卖价格
    private Integer premiumOverride;
    private Integer regularOverride;
    // 是否显示
    private Boolean showInMarket;
    // 开始时间
    private DateField startDate;
    // 是否是支持者
    private Boolean supporterPack;
    @JsonProperty("TypeName")
    private String typeName;
}
