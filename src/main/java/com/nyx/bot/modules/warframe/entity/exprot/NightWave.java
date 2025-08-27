package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 电波任务
 */
@NoArgsConstructor
@Data
@Entity
@Table
public class NightWave {
    @Id
    @NotEmpty(message = "unique_name.not.empty")
    @JsonProperty("uniqueName")
    private String uniqueName;
    /**
     * 任务名称
     */
    @JsonProperty("name")
    private String name;
    /**
     * 任务描述
     */
    @JsonProperty("description")
    private String description;
    /**
     * 声望数值
     */
    @JsonProperty("standing")
    private Integer standing;
    /**
     * 任务数量
     */
    @JsonProperty("required")
    private Integer required;

    public String getDescription() {
        return description.replace("|COUNT|", required.toString());
    }

    /**
     * 是否是每日任务
     */
    public Boolean isDailyTasks() {
        return standing == 1000;
    }

    /**
     * 是否是周任务
     */
    public Boolean isWeeklyTasks() {
        return standing == 4500;
    }

    /**
     * 是否是精英任务
     */
    public Boolean isEliteMissions() {
        return standing == 7000;
    }
}
