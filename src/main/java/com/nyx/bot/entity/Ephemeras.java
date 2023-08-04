package com.nyx.bot.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe 幻纹
 */
@Data
@Entity
@Table(name = "ephemeras")
public class Ephemeras {

    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(length = 50)
    String ephemerasId;
    @Column(length = 80)
    String icon;
    @Column(length = 20)
    String iconFormat;
    @Column(length = 50)
    String itemName;
    @Column(length = 80)
    String animation;
    @Column(length = 20)
    String element;
    @Column(length = 40)
    String urlName;
    @Column(length = 80)
    String thumb;

}
