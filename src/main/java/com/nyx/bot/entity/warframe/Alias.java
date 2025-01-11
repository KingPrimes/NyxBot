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
    @NotEmpty(message = "中文不能为空")
    String cn;
    @JsonProperty("en")
    @NotEmpty(message = "英文不能为空")
    String en;

    public boolean isValidEnglish() {
        return en.matches("^[a-zA-Z0-9_&]+$");
    }
}
