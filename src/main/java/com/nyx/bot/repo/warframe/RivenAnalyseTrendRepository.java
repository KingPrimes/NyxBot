package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenAnalyseTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RivenAnalyseTrendRepository extends JpaRepository<RivenAnalyseTrend, Long>, JpaSpecificationExecutor<RivenAnalyseTrend>, PagingAndSortingRepository<RivenAnalyseTrend, Long> {
    RivenAnalyseTrend findByName(String name);
}
