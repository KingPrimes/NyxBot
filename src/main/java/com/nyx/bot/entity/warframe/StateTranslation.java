package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.utils.StringUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Table(name = "state_translation", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "uniqueName"}))
@JsonView(Views.View.class)
public class StateTranslation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    //唯一自增ID
    String id;
    // 解释
    @JsonProperty("description")
    String description;
    // 名称
    @JsonProperty("name")
    @NotEmpty(message = "state.name.not.empty")
    String name;
    // 类型
    @JsonProperty("type")
    StateTypeEnum type;
    // 唯一名词
    @JsonProperty("unique_name")
    @NotEmpty(message = "state.unique_name.not.empty")
    String uniqueName;
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

    @PrePersist
    protected void onCreate() {
        if (id != null && id.isEmpty()) {
            id = null;
        }
    }
}
