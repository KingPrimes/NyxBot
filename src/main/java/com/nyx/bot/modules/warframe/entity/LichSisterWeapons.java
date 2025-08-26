package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Warframe 信条/赤毒 武器
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"url_name", "item_name"}))
@JsonView(Views.View.class)
@Accessors(chain = true)
public class LichSisterWeapons extends BaseEntity {
    @Id
    @JsonProperty("id")
    //唯一字符串武器ID
    String id;
    //在URL路径中的名称
    @JsonProperty("slug")
    String slug;
    //武器图标
    @JsonProperty("icon")
    String icon;
    //Lotus 名称
    @JsonProperty("gameRef")
    String gameRef;
    //武器段位限制
    @JsonProperty("reqMasteryRank")
    Integer reqMasteryRank;
    //武器名称
    @JsonProperty("name")
    String name;
    @JsonProperty("thumb")
    String thumb;
}
