package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.OrdersItems;
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
     * @param name 物品名称
     * @return 结果
     */
    @Query(value = "select * from ORDERS_ITEMS where upper(replace(NAME,' ','')) like upper(replace('%'||:#{#i}||'%',' ','')) and SLUG regexp '(.*set)?' group by NAME limit 1", nativeQuery = true)
    Optional<OrdersItems> findByItemNameLike(@Param("i") String name);

    /**
     * 根据物品名称模糊查询
     *
     * @param name 物品名称
     * @return 结果列表
     */

    @Query(value = "select * from ORDERS_ITEMS where upper(replace(NAME,' ','')) like upper(replace('%'||:#{#i}||'%',' ',''))", nativeQuery = true)
    List<OrdersItems> findByItemNameLikeToList(@Param("i") String name);

    /**
     * 正则查询
     *
     * @param regex 正则表达式
     */
    @Query(value = "select * from ORDERS_ITEMS where upper(replace(NAME,' ','')) regexp upper(replace(:#{#r},' ','')) limit 1", nativeQuery = true)
    Optional<OrdersItems> findByItemNameRegex(@Param("r") String regex);

    @Query("select o from OrdersItems o where (:name is null or LOWER(o.name) like LOWER(concat('%', :name, '%')))")
    Page<OrdersItems> findAllPageable(String name, Pageable pageable);

}
