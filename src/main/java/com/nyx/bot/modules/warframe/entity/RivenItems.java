package com.nyx.bot.modules.warframe.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Warframe Riven 数据
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table
@JsonView(Views.View.class)
@Accessors(chain = true)
public class RivenItems extends BaseEntity {

    @Id
    @JsonProperty("id")
    //紫卡ID
    String id;

    @JsonProperty("slug")
    //URL name
    String slug;

    @JsonProperty("gameRef")
    //Lotus 名称
    String gameRef;

    @JsonProperty("group")
    @Column(name = "`group`")
    //分组
    String group;

    @JsonProperty("rivenType")
    //紫卡类型
    String rivenType;

    @JsonProperty("disposition")
    //倾向数值
    Double disposition;

    @JsonProperty("reqMasteryRank")
    //等级限制
    Integer reqMasteryRank;

    @JsonProperty("name")
    //物品名称
    String name;

    @JsonProperty("icon")
    //图标
    String icon;

    @JsonProperty("thumb")
    //缩略图
    String thumb;

    @Transient
    List<RivenItems> items;


}
