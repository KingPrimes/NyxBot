package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.InternationalizedNotEmpty;
import com.nyx.bot.core.Views;
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
@JsonView(Views.View.class)
public class Alias extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")

    Long id;
    @JsonProperty("cn")
    @InternationalizedNotEmpty(message = "alias.cn.not.empty")
    String cn;
    @JsonProperty("en")
    @InternationalizedNotEmpty(message = "alias.en.not.empty")
    String en;

    public boolean isValidEnglish() {
        return en.matches("^([a-zA-Z]+)(_&)?([0-9]+)?([a-zA-Z]+)?$");
    }

    public boolean isValidChinese() {
        return cn.matches("^[\\u4e00-\\u9fa5]+$");
    }
}
