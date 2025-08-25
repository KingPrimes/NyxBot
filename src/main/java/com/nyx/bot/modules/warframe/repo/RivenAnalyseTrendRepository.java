package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RivenAnalyseTrendRepository extends JpaRepository<RivenAnalyseTrend, Long>, JpaSpecificationExecutor<RivenAnalyseTrend> {
    Optional<RivenAnalyseTrend> findByName(String name);
}
