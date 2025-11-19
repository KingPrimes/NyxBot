package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kingprimes.model.enums.RarityEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "relic_rewards")
@NoArgsConstructor
@Data
public class RelicRewards {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JsonProperty("rewardName")
    private String rewardName;

    @JsonProperty("rarity")
    private RarityEnum rarity;

    @JsonProperty("tier")
    private Integer tier;

    @JsonProperty("itemCount")
    private Integer itemCount;

    public String getRewardName() {
        if (itemCount > 1) return itemCount + "X" + rewardName;
        return rewardName;
    }
}
