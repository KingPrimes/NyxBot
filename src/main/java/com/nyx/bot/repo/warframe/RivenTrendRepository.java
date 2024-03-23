package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenTrend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RivenTrendRepository extends JpaRepository<RivenTrend, Long>, JpaSpecificationExecutor<RivenTrend> {

    @Query("select new com.nyx.bot.entity.warframe.RivenTrend(rt.id,rt.trendName,rt.newDot,rt.newNum,rt.oldDot,rt.oldNum,rt.isDate,rt.type,t.cn) from RivenTrend rt left join Translation t on rt.trendName = t.en where rt.trendName like concat('%',:trendName,'%') ")
    List<RivenTrend> findLikeTrendName(@Param("trendName") String trendName);


    RivenTrend findByTrendName(String name);

    @Query("select new com.nyx.bot.entity.warframe.RivenTrend(r.id,r.trendName,r.newDot,r.newNum,r.oldDot,r.oldNum,r.isDate,r.type,t.cn) from RivenTrend r left join Translation t on r.trendName = t.en where (:name is null or t.cn like concat('%',:name,'%'))")
    Page<RivenTrend> findAllPageable(String name, Pageable pageable);
}
