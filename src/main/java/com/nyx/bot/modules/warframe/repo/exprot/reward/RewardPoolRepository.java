package com.nyx.bot.modules.warframe.repo.exprot.reward;

import com.nyx.bot.modules.warframe.entity.exprot.reward.RewardPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 奖励池
 */
@Repository
public interface RewardPoolRepository extends JpaRepository<RewardPool, String>, JpaSpecificationExecutor<RewardPool> {
}
