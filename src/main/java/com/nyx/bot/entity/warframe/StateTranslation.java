package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.utils.StringUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"NAME", "UNIQUE_NAME"}))
@JsonView(Views.View.class)
public class StateTranslation extends BaseEntity {
    // 唯一名词
    @Id
    @JsonProperty("uniqueName")
    @NotEmpty(message = "unique_name.not.empty")
    String uniqueName;
    // 名称
    @JsonProperty("name")
    @NotEmpty(message = "state.name.not.empty")
    String name;
    // 解释
    @JsonProperty("description")
    String description;
    // 类型
    @JsonProperty("type")
    StateTypeEnum type;
    // 能否交易
    @JsonProperty("tradable")
    Boolean tradable;

    // /Lotus/StoreItems/Types/BoosterPacks/BaroTreasureBox
    // 虚空余货

    public StateTranslation(String description, String name, StateTypeEnum type, String uniqueName, Boolean tradable) {
        this.description = description;
        this.name = name;
        this.type = type;
        this.uniqueName = uniqueName;
        this.tradable = tradable;
    }

    public StateTranslation() {
    }

    public StateTranslation(StateTranslation stateTranslation) {
        this.description = stateTranslation.description;
        this.name = stateTranslation.name;
        this.type = stateTranslation.type;
        this.uniqueName = stateTranslation.uniqueName;
        this.tradable = stateTranslation.tradable;
    }

    @JsonIgnore
    public String getEquation() {
        return StringUtils.trimEx(name.toUpperCase()) + StringUtils.trimEx(uniqueName.toUpperCase());
    }
}
