package com.nyx.bot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe 幻纹
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"urlName","itemName"}))
public class Ephemeras {

    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(length = 50)
    @JsonProperty("id")
    String ephemerasId;
    @Column(length = 80)
    @JsonProperty("icon")
    String icon;
    @Column(length = 20)
    @JsonProperty("icon_format")
    String iconFormat;
    @JsonProperty("item_name")
    @Column(length = 50)
    String itemName;
    @JsonProperty("animation")
    @Column(length = 80)
    String animation;
    @JsonProperty("element")
    @Column(length = 20)
    String element;
    @JsonProperty("url_name")
    @Column(length = 40)
    String urlName;
    @JsonProperty("thumb")
    @Column(length = 80)
    String thumb;

}
