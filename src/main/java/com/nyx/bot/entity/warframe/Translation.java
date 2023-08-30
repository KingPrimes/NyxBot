package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用于翻译Warframe 中英文
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cn", "en"}))
public class Translation extends BaseEntity {
    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JsonProperty("cn")
    String cn;
    @JsonProperty("en")
    String en;
    @JsonProperty("is_prime")
    Boolean isPrime;
    @JsonProperty("is_set")
    Boolean isSet;
}
