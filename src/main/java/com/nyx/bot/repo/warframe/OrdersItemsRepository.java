package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.OrdersItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrdersItem 用于查询Warframe.Market 上面的物品,
 * 根据指令匹配本地数据
 * Jpa 操作数据源 接口
 */
@Repository
public interface OrdersItemsRepository extends JpaRepository<OrdersItems, String>, JpaSpecificationExecutor<OrdersItems> {

    /**
     * 根据物品名称模糊查询
     *
     * @param itemName 物品名称
     * @return 结果
     */
    @Query(value = "select * from ORDERS_ITEMS where upper(replace(ITEM_NAME,' ','')) like upper(replace('%'||:#{#i}||'%',' ','')) and URL_NAME regexp '(.*set)?' group by ITEM_NAME limit 1", nativeQuery = true)
    Optional<OrdersItems> findByItemNameLike(@Param("i") String itemName);

    /**
     * 根据物品名称模糊查询
     *
     * @param itemName 物品名称
     * @return 结果列表
     */

    @Query(value = "select * from ORDERS_ITEMS where upper(replace(ITEM_NAME,' ','')) like upper(replace('%'||:#{#i}||'%',' ',''))", nativeQuery = true)
    List<OrdersItems> findByItemNameLikeToList(@Param("i") String itemName);

    /**
     * 正则查询
     *
     * @param regex 正则表达式
     */
    @Query(value = "select * from ORDERS_ITEMS where upper(replace(ITEM_NAME,' ','')) regexp upper(replace(:#{#r},' ','')) limit 1", nativeQuery = true)
    Optional<OrdersItems> findByItemNameRegex(@Param("r") String regex);

    @Query("select o from OrdersItems o where (:itemName is null or LOWER(o.itemName) like LOWER(concat('%', :itemName, '%')))")
    Page<OrdersItems> findAllPageable(String itemName, Pageable pageable);

}
