package com.nyx.bot.repo.warframe.exprot.reward;

import com.nyx.bot.entity.warframe.exprot.reward.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardRepository extends JpaRepository<Reward, String>, JpaSpecificationExecutor<Reward>  {
}
