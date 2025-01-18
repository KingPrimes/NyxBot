package com.nyx.bot.entity.warframe;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Warframe Riven 数据
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "urlName"))
@JsonView(Views.View.class)
public class RivenItems extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long rivenId;
    @JsonProperty("id")
    @Column(length = 50)
    //紫卡ID
    String id;
    @JsonProperty("item_name")
    @Column(length = 50)
    //物品名称
    String itemName;
    @JsonProperty("url_name")
    @Column(length = 50)
    //URL name
    String urlName;
    @JsonProperty("riven_type")
    @Column(length = 50)
    //紫卡类型
    String rivenType;
    @JsonProperty("icon")
    @Column(length = 120)
    //图标
    String icon;
    @JsonProperty("icon_format")
    @Column(length = 80)
    //图标类型
    String iconFormat;
    @Column(length = 30)
    @JsonProperty("group")
    //分组
    String type;
    @JsonProperty("thumb")
    @Column(length = 120)
    //缩略图
    String thumb;
    //段位限制
    @JsonProperty("mastery_level")
    Integer masteryLevel;

    @Transient
    List<RivenItems> items;


}
