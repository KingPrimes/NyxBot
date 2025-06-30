package com.nyx.bot.entity.warframe.exprot.reward;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

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

    Rarity rarity;

    @Getter
    public enum Rarity {
        Common("常见"),
        Uncommon("罕见"),
        Rare("稀有"),
        ;
        final String name;

        Rarity(String s) {
            name = s;
        }
    }
}
