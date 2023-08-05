package com.nyx.bot.repo;

import com.nyx.bot.entity.OrdersItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * OrdersItem 用于查询Warframe.Market 上面的物品,
 * 根据指令匹配本地数据
 * Jpa 操作数据源 接口
 */
@Repository
public interface OrdersItemsRepository extends JpaRepository<OrdersItems,Long> {}
