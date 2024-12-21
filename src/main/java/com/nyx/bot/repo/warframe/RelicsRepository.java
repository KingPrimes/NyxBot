package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Relics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelicsRepository extends JpaRepository<Relics, String>, JpaSpecificationExecutor<Relics>, PagingAndSortingRepository<Relics, String> {

    /**
     * 根据名称查询
     *
     * @param name 遗物名称
     * @return 列表
     */
    List<Relics> findByRelicName(String name);

    /**
     * 根据名称和纪元查询
     *
     * @param name 遗物名称
     * @param tier 纪元
     * @return 列表
     */
    List<Relics> findByRelicNameAndTier(String name, String tier);

    /**
     * 根据纪元查询
     *
     * @param tier 纪元
     * @return 列表
     */
    List<Relics> findByTier(String tier);

    /**
     * 分页条件查询
     *
     * @param re 条件
     * @param p  分页
     * @return Page<Relics>
     */
    @Query("select r from Relics r where (:#{#re.relicName} is null or r.relicName = :#{#re.relicName})")
    Page<Relics> findAllPageable(@Param("re") Relics re, Pageable p);
}
