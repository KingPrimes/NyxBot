package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 紫卡倾向库
 */
@EqualsAndHashCode(callSuper = true, of = {"trendName", "oldNum", "newNum"})
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "trendName"))
@AllArgsConstructor
@NoArgsConstructor
public class RivenTrend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long id;
    //武器名称
    @JsonProperty("trend_name")
    String trendName;
    //新的倾向 字符串
    @JsonProperty("new_dot")
    String newDot;
    //新的倾向 浮点
    @JsonProperty("new_num")
    Double newNum;
    //旧的倾向 字符串
    @JsonProperty("old_dot")
    String oldDot;
    //旧的倾向 浮点
    @JsonProperty("old_num")
    Double oldNum;
    //武器类型
    @JsonProperty("type")
    RivenTrendTypeEnum type;

    //武器中文名称
    @Transient
    private String traCh;

    //此次更新得时间
    @Transient
    private String isDate;
}
