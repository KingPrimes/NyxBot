package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RivenTrendRepository extends JpaRepository<RivenTrend, Long>, JpaSpecificationExecutor<RivenTrend> {
}
