package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.OrdersItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OrdersItem 用于查询Warframe.Market 上面的物品,
 * 根据指令匹配本地数据
 * Jpa 操作数据源 接口
 */
@Repository
public interface OrdersItemsRepository extends JpaRepository<OrdersItems, Long>, JpaSpecificationExecutor<OrdersItems>, PagingAndSortingRepository<OrdersItems, Long> {

    /**
     * 添加数据
     */
    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO ORDERS_ITEMS(ITEM_NAME, ORDER_ID, THUMB, URL_NAME, VAULTED) VALUES (:#{#items.itemName},:#{#items.orderId},:#{#items.thumb},:#{#items.urlName},:#{#items.vaulted})"
            ,nativeQuery = true)
    Integer addOrdersItems(@Param("items") OrdersItems items);


    /**
     * 查询最大ID的数据
     */
    OrdersItems findTopByOrderByOidDesc();


    /**
     * 根据物品名称模糊查询
     *
     * @param itemName 物品名称
     * @return 结果
     */
    @Query(value = "select * from ORDERS_ITEMS where upper(replace(ITEM_NAME,' ','')) like upper(replace('%'||:#{#i}||'%',' ','')) and URL_NAME regexp '(.*set)?' group by ITEM_NAME limit 1", nativeQuery = true)
    OrdersItems findByItemNameLike(@Param("i") String itemName);

    /**
     * 根据物品名称模糊查询
     *
     * @param itemName 物品名称
     * @return 结果
     */

    @Query(value = "select * from ORDERS_ITEMS where upper(replace(ITEM_NAME,' ','')) like upper(replace('%'||:#{#i}||'%',' ',''))", nativeQuery = true)
    List<OrdersItems> findByItemNameLikeToList(@Param("i") String itemName);

    /**
     * 正则查询
     *
     * @param regex 正则表达式
     */
    @Query(value = "select * from ORDERS_ITEMS where upper(replace(ITEM_NAME,' ','')) regexp upper(replace(:#{#r},' ','')) limit 1", nativeQuery = true)
    OrdersItems findByItemNameRegex(@Param("r") String regex);

}
