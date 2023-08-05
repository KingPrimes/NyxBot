package com.nyx.bot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe 信条/赤毒 武器
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"urlName","itemName"}))
public class Weapons {
    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JsonProperty("id")
    String weaponId;
    @JsonProperty("url_name")
    String urlName;
    @JsonProperty("icon")
    String icon;
    @JsonProperty("icon_format")
    String iconForMat;
    @JsonProperty("thumb")
    String thumb;
    @JsonProperty("item_name")
    String itemName;
}
