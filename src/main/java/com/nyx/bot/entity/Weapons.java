package com.nyx.bot.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe 信条/赤毒 武器
 */
@Data
@Entity
@Table(name = "weapons")
public class Weapons {
    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String weaponId;
    String urlName;
    String icon;
    String iconForMat;
    String thumb;
    String itemName;
}
