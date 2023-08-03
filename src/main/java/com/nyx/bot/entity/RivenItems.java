package com.nyx.bot.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe Riven 数据
 */
@Data
@Entity
@Table(name = "riven_items")
public class RivenItems {

    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(length = 50)
    String rivenId;
    @Column(length = 50)
    String itemName;
    @Column(length = 50)
    String urlName;
    @Column(length = 50)
    String rivenType;
    @Column(length = 80)
    String icon;
    @Column(length = 80)
    String iconFormat;

    @Column(length = 30)
    @JsonProperty("group")
    String type;

    @Column(length = 80)
    String thumb;

    Integer masteryLevel;


}
