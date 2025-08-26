package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Warframe Orders Items数据
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table
@JsonView(Views.View.class)
@Accessors(chain = true)
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
