package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Warframe 物品的别名
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "cn"))
public class Alias extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    Long id;
    @JsonProperty("cn")
    String cn;
    @JsonProperty("en")
    String en;
}
