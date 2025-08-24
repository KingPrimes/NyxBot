package com.nyx.bot.repo.warframe.exprot;

import com.nyx.bot.entity.warframe.exprot.Relics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unused")
public interface RelicsRepository extends JpaRepository<Relics, String>, JpaSpecificationExecutor<Relics> {
    /**
     * 方法名约定实现模糊查询：包含指定奖励名称的遗物
     * 等效于 JPQL: SELECT r FROM Relics r JOIN r.relicRewards rr WHERE rr.rewardName LIKE %:rewardName%
     *
     * @param rewardName 奖励名称
     */

    List<Relics> findByRelicRewardsRewardNameContaining(String rewardName);



    /**
     *  扩展：开头匹配（如"Prime%"）
     * @param prefix 前缀
     */
    List<Relics> findByRelicRewardsRewardNameStartingWith(String prefix);

    /**
     * 扩展：结尾匹配（如"%Blueprint"）
     * @param suffix 后缀
     */
    List<Relics> findByRelicRewardsRewardNameEndingWith(String suffix);

    /**
     * 分页条件查询
     * @param re 条件
     * @param p  分页
     * @return Page<Relics>
     */
    @Query("select r from Relics r where (:#{#re.name} is null or r.name = :#{#re.name})")
    Page<Relics> findAllPageable(@Param("re") Relics re, Pageable p);

    /**
     * 根据名称查询
     * @param name 遗物名称
     */
    List<Relics> findByName(String name);

    /**
     * 模糊查询
     * @param name 遗物名称
     */
    List<Relics> findByNameContaining(String name);
}
