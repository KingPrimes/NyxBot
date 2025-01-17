package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用于翻译Warframe 中英文
 */
@EqualsAndHashCode(callSuper = false, exclude = {"cn", "en", "isPrime", "isSet"})
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cn", "en"}))
@JsonView(Views.View.class)
public class Translation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long id;
    //中文名称
    @JsonProperty("cn")
    @NotEmpty(message = "{translation.cn.not.empty}")
    String cn;
    //英文名称
    @JsonProperty("en")
    @NotEmpty(message = "{translation.en.not.empty}")
    String en;
    //是否是 Prime 版本
    @JsonProperty("is_prime")
    Boolean isPrime;
    //是否是一套物品
    @JsonProperty("is_set")
    Boolean isSet;


}
