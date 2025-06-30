package com.nyx.bot.entity.warframe.exprot.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

/**
 * 奖励池
 */
@Data
@Entity
@Table
public class RewardPool {
    /**
     * 唯一名称
     */
    @Id
    @JsonProperty("uniqueName")
    @NotEmpty(message = "unique_name.not.empty")
    String uniqueName;

    @OneToMany(mappedBy = "rewards", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonProperty("rewards")
    Set<Reward> rewards;

}
