package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Warframe 幻纹
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"url_name", "item_name"}))
@JsonView(Views.View.class)
public class Ephemeras extends BaseEntity {

    @Id
    @JsonProperty("id")
    String id;
    @Column(length = 120)
    @JsonProperty("icon")
    String icon;
    @Column(length = 20)
    @JsonProperty("icon_format")
    String iconFormat;
    @JsonProperty("item_name")
    @Column(length = 50)
    String itemName;
    @JsonProperty("animation")
    @Column(length = 120)
    String animation;
    @JsonProperty("element")
    @Column(length = 20)
    String element;
    @JsonProperty("url_name")
    @Column(length = 40)
    String urlName;
    @JsonProperty("thumb")
    @Column(length = 120)
    String thumb;

}
