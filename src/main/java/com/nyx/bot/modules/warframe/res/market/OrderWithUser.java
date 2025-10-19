package com.nyx.bot.modules.warframe.res.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.res.enums.TransactionEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * Market Order 订单信息
 */
@Data
@Accessors(chain = true)
public class OrderWithUser {
    /**
     * 订单ID
     */
    @JsonProperty("id")
    String id;
    /**
     * 交易类型
     */
    @JsonProperty("type")
    TransactionEnum type;
    /**
     * 订单价格
     */
    @JsonProperty("platinum")
    Integer platinum;
    /**
     * 订单数量
     */
    @JsonProperty("quantity")
    Integer quantity;
    /**
     * 单价
     */
    @JsonProperty("perTrade")
    Integer perTrade;
    /**
     * 等级
     */
    @JsonProperty("rank")
    Integer rank;
    /**
     * 槽位
     */
    @JsonProperty("charges")
    Integer charges;
    /**
     * 子类型
     */
    @JsonProperty("subtype")
    String subtybe;
    /**
     * 黄星数
     * 在查询阿耶檀识宝物时使用
     */
    @JsonProperty("amberStars")
    Integer amberStars;
    /**
     * 蓝星数
     * 在查询阿耶檀识宝物时使用
     */
    @JsonProperty("cyanStars")
    Integer cyanStars;
    /**
     * 阿耶檀识宝物分解后获得的内融核心数
     */
    @JsonProperty("vosfor")
    Integer vosfor;
    /**
     * 订单是否可见
     * true 可见
     * false 不可见
     */
    @JsonProperty("visible")
    Boolean visible;
    /**
     * 创建时间
     */
    @JsonProperty("createdAt")
    Instant createdAt;
    /**
     * 修改时间
     */
    @JsonProperty("updatedAt")
    Instant updatedAt;
    /**
     * 物品ID
     * 通过物品ID查询物品信息
     */
    @JsonProperty("itemId")
    String itemId;
    /**
     * 订单用户信息
     */
    @JsonProperty("user")
    MarketUser user;
}
