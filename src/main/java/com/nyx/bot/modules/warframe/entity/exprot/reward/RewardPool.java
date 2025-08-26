package com.nyx.bot.modules.warframe.entity.exprot.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "reward_pool_reward", joinColumns = @JoinColumn(name = "rewards"), inverseJoinColumns = @JoinColumn(name = "reward_id"))
    @JsonProperty("rewards")
    List<Reward> rewards;

}
