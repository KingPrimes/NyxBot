package com.nyx.bot.modules.warframe.entity.exprot.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 奖励池
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table
public class RewardPool extends BaseEntity {
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
