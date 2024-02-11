package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenTrend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RivenTrendRepository extends JpaRepository<RivenTrend, Long>, JpaSpecificationExecutor<RivenTrend> {

    @Query(value = "select new RivenTrend(t1.id,t1.trendName,t1.newDot,t1.newNum,t1.oldDot,t1.oldNum,t1.type,t2.cn,'') from RivenTrend t1 left join Translation t2 on t1.trendName = t2.en")
    Page<RivenTrend> findRivenTrendAndTra(Specification<RivenTrend> specification, Pageable pageable);
}
