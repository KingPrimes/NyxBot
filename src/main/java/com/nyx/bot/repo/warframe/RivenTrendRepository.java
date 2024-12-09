package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenTrend;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface RivenTrendRepository extends JpaRepository<RivenTrend, Long>, JpaSpecificationExecutor<RivenTrend> {

    @Query("select new com.nyx.bot.entity.warframe.RivenTrend(rt.id,rt.trendName,rt.newDot,rt.newNum,rt.oldDot,rt.oldNum,rt.isDate,rt.type,t.cn) from RivenTrend rt left join Translation t on rt.trendName = t.en where rt.trendName like concat('%',:trendName,'%') ")
    List<RivenTrend> findLikeTrendName(@Param("trendName") String trendName);


    Optional<RivenTrend> findByTrendName(String name);

    @Query("select new com.nyx.bot.entity.warframe.RivenTrend(r.id,r.trendName,r.newDot,r.newNum,r.oldDot,r.oldNum,r.isDate,r.type,t.cn) from RivenTrend r left join Translation t on r.trendName = t.en where (:name is null or t.cn like concat('%',:name,'%'))")
    Page<RivenTrend> findAllPageable(String name, Pageable pageable);

    /**
     * 获取最新更新的紫卡倾向 列表
     */
    @Cacheable(value = "rivenDisUpdate")
    @CacheEvict(value = "rivenDisUpdate", allEntries = true)
    @Query(value = "select new com.nyx.bot.entity.warframe.RivenTrend(r.id,r.trendName,r.newDot,r.newNum,r.oldDot,r.oldNum,r.isDate,r.type,t.cn) from RivenTrend r left join Translation t on r.trendName = t.en where r.isDate IS NOT NULL and r.isDate = (select Max(isDate) from RivenTrend where isDate IS NOT NULL)")
    List<RivenTrend> findRivenDisUpdate();

    /***
     * 获取最新更新的时间
     */
    @Query(value = "select Max(isDate) from RivenTrend where isDate IS NOT NULL")
    Optional<Timestamp> findRivenTrendByIsDate();
}
