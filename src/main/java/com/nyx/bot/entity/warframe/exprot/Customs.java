package com.nyx.bot.entity.warframe.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 外观
 */
@Data
@Entity
@Table
public class Customs {
    /**
     * 唯一名称
     */
    @Id
    @JsonProperty("uniqueName")
    @NotEmpty(message = "unique_name.not.empty")
    String uniqueName;
    /**
     * 名称
     */
    @JsonProperty("name")
    String name;
    /**
     * 是否是保密
     */
    @JsonProperty("codexSecret")
    Boolean codexSecret;

    /**
     * 是否排除
     */
    @JsonProperty("excludeFromCodex")
    Boolean excludeFromCodex;

    /**
     * 描述
     */
    @JsonProperty("description")
    String description;
}
