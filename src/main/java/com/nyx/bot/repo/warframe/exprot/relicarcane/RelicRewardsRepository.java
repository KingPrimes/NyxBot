package com.nyx.bot.repo.warframe.exprot.relicarcane;

import com.nyx.bot.entity.warframe.exprot.relicarcane.RelicRewards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 外观
 */
@Repository
public interface RelicRewardsRepository extends JpaRepository<RelicRewards, String>, JpaSpecificationExecutor<RelicRewards> {
}
