package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LichSisterWeaponsRepository extends NameRegexJpaRepository<LichSisterWeapons, String>, JpaSpecificationExecutor<LichSisterWeapons> {

    /**
     * 根据武器名称精确查询
     *
     * @param name 武器名称
     * @return 匹配的武器实体
     */
    Optional<LichSisterWeapons> findByName(String name);

    /**
     * 根据武器名称模糊查询并返回第一个结果
     *
     * @param name 武器名称
     * @return 匹配的第一个武器实体
     */
    default Optional<LichSisterWeapons> findFirstByNameContaining(String name) {
        List<LichSisterWeapons> results = findByNameContaining(name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    /**
     * 根据武器名称正则查询
     *
     * @param regex 武器名称正则
     * @return 匹配的武器实体
     */
    @Query(value = "select l from LICH_SISTER_WEAPONS l WHERE UPPER(REPLACE(l.NAME,' ','')) REGEXP UPPER(replace(:#{#regex},' ',''))", nativeQuery = true)
    Optional<LichSisterWeapons> findByNameRegex(String regex);

    List<LichSisterWeapons> findByNameContaining(String name);
}
