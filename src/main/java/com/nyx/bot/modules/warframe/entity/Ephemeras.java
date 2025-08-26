package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Warframe 幻纹
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table
@JsonView(Views.View.class)
@Accessors(chain = true)
public class Ephemeras extends BaseEntity {

    @Id
    @JsonProperty("id")
    String id;
    @Column(length = 50)
    @JsonProperty("slug")
    String slug;
    @JsonProperty("gameRef")
    String gameRef;
    @JsonProperty("animation")
    @Column(length = 120)
    String animation;
    @JsonProperty("element")
    @Column(length = 20)
    String element;
    @JsonProperty("name")
    @Column(length = 80)
    String name;
    @JsonProperty("icon")
    @Column(length = 120)
    String icon;
    @JsonProperty("thumb")
    @Column(length = 120)
    String thumb;

}
