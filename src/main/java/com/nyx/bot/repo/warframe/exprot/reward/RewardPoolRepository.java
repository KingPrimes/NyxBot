package com.nyx.bot.repo.warframe.exprot.reward;

import com.nyx.bot.entity.warframe.exprot.reward.RewardPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 奖励池
 */
@Repository
public interface RewardPoolRepository extends JpaRepository<RewardPool, String>, JpaSpecificationExecutor<RewardPool> {
}
