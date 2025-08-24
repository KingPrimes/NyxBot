package com.nyx.bot.repo.warframe.exprot;

import com.nyx.bot.entity.warframe.exprot.RelicRewards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelicRewardsRepository extends JpaRepository<RelicRewards, String> {

}
