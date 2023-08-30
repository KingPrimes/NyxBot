package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 紫卡倾向库
 */
@EqualsAndHashCode(callSuper = true, of = {"trendName", "oldNum", "newNum"})
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "trendName"))
public class RivenTrend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JsonProperty("trend_name")
    String trendName;
    @JsonProperty("new_dot")
    String newDot;
    @JsonProperty("new_num")
    Double newNum;
    @JsonProperty("old_dot")
    String oldDot;
    @JsonProperty("old_num")
    Double oldNum;
    @JsonProperty("type")
    String type;
}
