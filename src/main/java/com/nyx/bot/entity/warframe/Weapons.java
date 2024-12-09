package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Warframe 信条/赤毒 武器
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"url_name", "item_name"}))
public class Weapons extends BaseEntity {
    @Id
    @JsonProperty("id")
    //唯一字符串武器ID
    String id;
    //在URL路径中的名称
    @JsonProperty("url_name")
    String urlName;
    //武器图标
    @JsonProperty("icon")
    String icon;
    //图标类型
    @JsonProperty("icon_format")
    String iconForMat;
    //武器缩略图
    @JsonProperty("thumb")
    String thumb;
    //武器名称
    @JsonProperty("item_name")
    String itemName;
}
