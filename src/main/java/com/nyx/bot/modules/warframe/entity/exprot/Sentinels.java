package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 守护|宠物
 */
@NoArgsConstructor
@Data
@Entity
@Table
public class Sentinels {

    @Id
    @NotEmpty(message = "unique_name.not.empty")
    @JsonProperty("uniqueName")
    private String uniqueName;

    @JsonProperty("name")
    private String name;
    /**
     * 生命值
     */
    @JsonProperty("health")
    private Integer health;
    /**
     * 护盾
     */
    @JsonProperty("shield")
    private Integer shield;
    /**
     * 护甲
     */
    @JsonProperty("armor")
    private Integer armor;
    /**
     * 耐力
     */
    @JsonProperty("stamina")
    private Integer stamina;
    /**
     * 能量
     */
    @JsonProperty("power")
    private Integer power;

    @JsonProperty("codexSecret")
    private Boolean codexSecret;

    @JsonProperty("excludeFromCodex")
    private Boolean excludeFromCodex;
    /**
     * 描述
     */
    @JsonProperty("description")
    private String description;
    /**
     * 类别
     */
    @JsonProperty("productCategory")
    private String productCategory;
}
