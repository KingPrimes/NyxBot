package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.utils.StringUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"NAME", "UNIQUE_NAME"}))
@JsonView(Views.View.class)
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @Column(columnDefinition = "text")
    @JsonProperty("description")
    String description;
    // 类型
    @JsonProperty("type")
    StateTypeEnum type;
    // 能否交易
    @JsonProperty("parentName")
    String parentName;

    public StateTranslation(String description, String name, StateTypeEnum type, String uniqueName, String parentName) {
        this.description = description;
        this.name = name;
        this.type = type;
        this.uniqueName = uniqueName;
        this.parentName = parentName;
    }

    public StateTranslation() {
    }

    public StateTranslation(StateTranslation stateTranslation) {
        this.description = stateTranslation.description;
        this.name = stateTranslation.name;
        this.type = stateTranslation.type;
        this.uniqueName = stateTranslation.uniqueName;
        this.parentName = stateTranslation.parentName;
    }

    public String getDescription() {
        if (description == null) {
            return "";
        }
        return description;
    }

    public void setDescription(Object description) {
        if (description instanceof List<?> descList) {
            if (!descList.isEmpty()) {
                this.description = descList.getFirst().toString();
            } else {
                this.description = "";
            }
        } else {
            this.description = description.toString();
        }
    }

    @JsonIgnore
    public String getEquation() {
        return StringUtils.trimEx(name.toUpperCase()) + StringUtils.trimEx(uniqueName.toUpperCase());
    }
}
