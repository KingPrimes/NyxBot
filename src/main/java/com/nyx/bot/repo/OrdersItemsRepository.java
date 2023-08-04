package com.nyx.bot.repo;

import com.nyx.bot.entity.OrdersItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersItemsRepository extends JpaRepository<OrdersItems,Long> {}
