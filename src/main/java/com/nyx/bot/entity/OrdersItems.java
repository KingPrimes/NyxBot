package com.nyx.bot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe Orders Items数据
 *
 */
@Data
@Entity
@Table(name = "orders_items",uniqueConstraints = @UniqueConstraint(name = "ordersItems",columnNames = {"itemName","urlName"}))
public class OrdersItems {

    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JsonProperty("id")
    @Column(length = 50)
    String orderId;
    @JsonProperty("url_name")
    @Column(length = 50)
    String urlName;
    @JsonProperty("item_name")
    @Column(length = 50)
    String itemName;
    @JsonProperty("thumb")
    @Column(length = 120)
    String thumb;
    @JsonProperty("vaulted")
    Boolean vaulted;

}
