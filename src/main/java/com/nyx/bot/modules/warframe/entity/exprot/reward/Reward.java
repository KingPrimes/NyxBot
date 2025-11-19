package com.nyx.bot.modules.warframe.entity.exprot.reward;

import io.github.kingprimes.model.enums.RarityEnum;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 具体奖励
 */
@Data
@Entity
@Table
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String item;

    RarityEnum rarity;

    Integer itemCount;


    public String getItem() {
        return item.replace("|COUNT|", itemCount.toString());
    }
}
