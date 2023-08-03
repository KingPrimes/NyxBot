package com.nyx.bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Warframe 信条/赤毒 武器
 */
@Data
@Entity
@Table(name = "weapons")
public class Weapons {

    @Id
    Long id;
    String weaponId;
    String urlName;
    String icon;
    String iconForMat;
    String thumb;
    String itemName;
}
