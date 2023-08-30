package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Warframe Orders Items数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "orders_items", uniqueConstraints = @UniqueConstraint(name = "ordersItems", columnNames = {"itemName", "urlName"}), indexes = @Index(columnList = "orderId"))
public class OrdersItems extends BaseEntity {

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
