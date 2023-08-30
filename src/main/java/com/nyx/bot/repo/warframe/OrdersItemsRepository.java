package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.OrdersItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * OrdersItem 用于查询Warframe.Market 上面的物品,
 * 根据指令匹配本地数据
 * Jpa 操作数据源 接口
 */
@Repository
public interface OrdersItemsRepository extends JpaRepository<OrdersItems, Long>, JpaSpecificationExecutor<OrdersItems> {

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
    OrdersItems findTopByOrderByIdDesc();
}
