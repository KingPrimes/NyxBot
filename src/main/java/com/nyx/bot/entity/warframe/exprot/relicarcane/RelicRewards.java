package com.nyx.bot.entity.warframe.exprot.relicarcane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.enums.RarityEnum;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table
public class RelicRewards {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @JsonProperty("rewardName")
    String rewardName;

    @JsonProperty("rarity")
    RarityEnum rarity;

    @JsonProperty("tier")
    Integer tier;

    @JsonProperty("itemCount")
    Integer itemCount;
}
