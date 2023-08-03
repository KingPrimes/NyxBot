package com.nyx.bot.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe Orders Items数据
 *
 */
@Data
@Entity
@Table(name = "orders_items")
public class OrdersItems {

    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(length = 50)
    String orderId;
    @Column(length = 50)
    String urlName;
    @Column(length = 50)
    String itemName;
    @Column(length = 80)
    String thumb;
    Boolean vaulted;

}
