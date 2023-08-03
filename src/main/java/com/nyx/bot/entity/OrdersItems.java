package com.nyx.bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
