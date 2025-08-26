package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlashSale {
    @JsonProperty("BogoBuy")
    private Integer bogoBuy;
    @JsonProperty("BogoGet")
    private Integer bogoGet;
    @JsonProperty("DailySaleGenerated")
    private Boolean dailySaleGenerated;
    // 折扣
    @JsonProperty("Discount")
    private Integer discount;
    // 结束时间
    @JsonProperty("EndDate")
    private DateField endDate;
    // 是否隐藏
    @JsonProperty("HideFromMarket")
    private Boolean hideFromMarket;
    // 售卖价格
    @JsonProperty("PremiumOverride")
    private Integer premiumOverride;
    @JsonProperty("RegularOverride")
    private Integer regularOverride;
    // 是否显示
    @JsonProperty("ShowInMarket")
    private Boolean showInMarket;
    // 开始时间
    @JsonProperty("StartDate")
    private DateField startDate;
    // 是否是支持者
    @JsonProperty("SupporterPack")
    private Boolean supporterPack;
    @JsonProperty("TypeName")
    private String typeName;
}
