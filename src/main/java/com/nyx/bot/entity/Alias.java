package com.nyx.bot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Warframe 物品的别名
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "aliasZhCn"))
public class Alias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("alias_id")
    Long id;
    @JsonProperty("alias_zh_cn")
    String aliasZhCn;
    @JsonProperty("alias_us_en")
    String aliasUsEn;
}
