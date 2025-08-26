package com.nyx.bot.modules.warframe.repo.exprot.reward;

import com.nyx.bot.modules.warframe.entity.exprot.reward.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardRepository extends JpaRepository<Reward, String>, JpaSpecificationExecutor<Reward>  {
}
