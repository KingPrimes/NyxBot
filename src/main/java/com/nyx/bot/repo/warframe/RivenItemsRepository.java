package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RivenItemsRepository extends JpaRepository<RivenItems, Long>, JpaSpecificationExecutor<RivenItems> {
    @Query(value = "select max(rivenId) from RivenItems")
    Long queryMaxId();

    Optional<RivenItems> findById(String id);

    /**
     * 分页查询
     *
     * @param itemName  物品名称
     * @param rivenType riven类型
     * @param pageable  分页
     * @return 结果集
     */
    @Query("SELECT r FROM RivenItems r WHERE (:itemName IS NULL OR LOWER(r.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) AND (:rivenType IS NULL OR r.rivenType = :rivenType)")
    Page<RivenItems> findAllPageable(String itemName, String rivenType, Pageable pageable);


    Optional<RivenItems> findByItemName(String itemName);

    @Query(value = "select * from RIVEN_ITEMS r where upper(replace(r.ITEM_NAME,' ','')) regexp upper(replace(:#{#regex},' ','')) limit 1", nativeQuery = true)
    Optional<RivenItems> findByItemNameRegex(String regex);

    @Query("select r from RivenItems r where LOWER(replace(r.itemName,' ','')) like LOWER(replace(CONCAT('%', :name, '%'),' ',''))")
    List<RivenItems> itemNameLikes(String name);

}
