package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 紫卡倾向库
 */
@EqualsAndHashCode(callSuper = false, of = {"trendName", "oldNum", "newNum"})
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "trendName"))
@AllArgsConstructor
@NoArgsConstructor
@JsonView(Views.View.class)
public class RivenTrend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long id;
    //武器名称
    @JsonProperty("trend_name")
    @NotEmpty(message = "武器名称不能为空")
    String trendName;
    //新的倾向 字符串
    @JsonProperty("new_dot")
    @NotEmpty(message = "新的倾向不能为空")
    String newDot;
    //新的倾向 浮点
    @JsonProperty("new_num")
    @NotEmpty(message = "新的倾向不能为空")
    Double newNum;
    //旧的倾向 字符串
    @JsonProperty("old_dot")
    @NotEmpty(message = "旧的倾向不能为空")
    String oldDot;
    //旧的倾向 浮点
    @JsonProperty("old_num")
    @NotEmpty(message = "旧的倾向不能为空")
    Double oldNum;
    //武器类型
    @JsonProperty("type")
    @NotEmpty(message = "武器类型不能为空")
    RivenTrendTypeEnum type;

    //此次更新得时间
    @JsonProperty("isDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotEmpty(message = "时间不能为空")
    private Timestamp isDate;

    //武器中文名称
    @Transient
    @JsonProperty("traCh")
    private String traCh;


    public RivenTrend(Long id, String trendName, String newDot, Double newNum, String oldDot, Double oldNum, Timestamp isDate, RivenTrendTypeEnum type, String traCh) {
        this.id = id;
        this.trendName = trendName;
        this.newDot = newDot;
        this.newNum = newNum;
        this.oldDot = oldDot;
        this.oldNum = oldNum;
        this.type = type;
        this.isDate = isDate;
        this.traCh = traCh;
    }
}
