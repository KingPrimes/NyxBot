package com.nyx.bot.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe Riven 数据
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "urlName"))
public class RivenItems {

    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JsonProperty("id")
    @Column(length = 50)
    String rivenId;
    @JsonProperty("item_name")
    @Column(length = 50)
    String itemName;
    @JsonProperty("url_name")
    @Column(length = 50)
    String urlName;
    @JsonProperty("riven_type")
    @Column(length = 50)
    String rivenType;
    @JsonProperty("icon")
    @Column(length = 80)
    String icon;
    @JsonProperty("icon_format")
    @Column(length = 80)
    String iconFormat;

    @Column(length = 30)
    @JsonProperty("group")
    String type;

    @JsonProperty("thumb")
    @Column(length = 80)
    String thumb;

    @JsonProperty("mastery_level")
    Integer masteryLevel;


}
