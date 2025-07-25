package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Warframe Orders Items数据
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "orders_items", uniqueConstraints = @UniqueConstraint(name = "ordersItems", columnNames = {"item_name", "url_name"}), indexes = @Index(columnList = "id"))
@JsonView(Views.View.class)
public class OrdersItems extends BaseEntity {

    @Id
    @JsonProperty("id")
    //唯一字符串ID
    String id;

    @JsonProperty("slug")
    @Column(length = 50)
    //URL name
    String slug;

    @JsonProperty("gameRef")
    String gameRef;


    // 可批量交易
    @JsonProperty("bulkTradable")
    Boolean bulkTradable;

    // 最大等级
    @JsonProperty("maxRank")
    Integer maxRank;

    // 杜卡币
    @JsonProperty("ducats")
    Integer ducats;

    @JsonProperty("name")
    //物品名称
    String name;

    // 图标
    @JsonProperty("icon")
    String icon;

    //缩略图
    @JsonProperty("thumb")
    String thumb;
    //
    @JsonProperty("subIcon")
    String subIcon;
}
