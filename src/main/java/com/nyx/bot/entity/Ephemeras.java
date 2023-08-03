package com.nyx.bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Warframe 幻纹
 */
@Data
@Entity
@Table(name = "ephemeras")
public class Ephemeras {

    @Id
    @Column(length = 50)
    String id;
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
