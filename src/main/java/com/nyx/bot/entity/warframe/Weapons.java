package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Warframe 信条/赤毒 武器
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"urlName", "itemName"}))
public class Weapons extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long id;
    @JsonProperty("id")
    //唯一字符串武器ID
    String weaponId;
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
