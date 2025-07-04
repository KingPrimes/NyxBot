package com.nyx.bot.entity.warframe.exprot.reward;

import com.nyx.bot.enums.RarityEnum;
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
}
