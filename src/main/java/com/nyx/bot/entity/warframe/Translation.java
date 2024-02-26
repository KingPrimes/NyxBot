package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用于翻译Warframe 中英文
 */
@EqualsAndHashCode(callSuper = false, exclude = {"cn", "en", "isPrime", "isSet"})
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cn", "en"}))
public class Translation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long id;
    //中文名称
    @JsonProperty("cn")
    String cn;
    //英文名称
    @JsonProperty("en")
    String en;
    //是否是 Prime 版本
    @JsonProperty("is_prime")
    Boolean isPrime;
    //是否是一套物品
    @JsonProperty("is_set")
    Boolean isSet;
}
