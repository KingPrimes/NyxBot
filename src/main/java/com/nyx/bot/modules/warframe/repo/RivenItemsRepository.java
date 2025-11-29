package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.RivenItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RivenItemsRepository extends NameRegexJpaRepository<RivenItems, String>, JpaSpecificationExecutor<RivenItems> {
    /**
     * 分页查询
     *
     * @param itemName  物品名称
     * @param rivenType riven类型
     * @param pageable  分页
     * @return 结果集
     */
    @Query("SELECT r FROM RivenItems r WHERE (:itemName IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND (:rivenType IS NULL OR r.rivenType = :rivenType)")
    Page<RivenItems> findAllPageable(String itemName, String rivenType, Pageable pageable);


    Optional<RivenItems> findByName(String name);

    @Query(value = "SELECT * FROM RIVEN_ITEMS r where upper(replace(r.name,' ','')) regexp upper(replace(:#{#regex},' ','')) limit 1", nativeQuery = true)
    Optional<RivenItems> findByNameRegex(String regex);

    @Query("select r from RivenItems r where LOWER(replace(r.name,' ','')) like LOWER(replace(CONCAT('%', :name, '%'),' ',''))")
    List<RivenItems> nameLikes(String name);

}
