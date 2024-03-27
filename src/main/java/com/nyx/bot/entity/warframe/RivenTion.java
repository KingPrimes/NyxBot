package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 紫卡词条参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"urlName"}))
public class RivenTion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long ids;
    //词条名称
    @JsonProperty("effect")
    String effect;
    //分组
    @JsonProperty("`group`")
    @Column(name = "`group`")
    String group;
    //仅负属性
    @JsonProperty("negative_only")
    Double negativeOnly;
    //
    @JsonProperty("negative_is_negative")
    Double positiveIsNegative;
    //前缀
    @JsonProperty("prefix")
    String prefix;
    //仅搜索
    @JsonProperty("search_only")
    Double searchOnly;
    //后缀
    @JsonProperty("suffix")
    String suffix;
    //
    @JsonProperty("units")
    String units;
    //URL Name
    @JsonProperty("url_name")
    String urlName;
    //可用武器类型
    @JsonProperty("exclusive_to")
    String exclusiveTo;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("ids", ids)
                .append("effect", effect)
                .append("group", group)
                .append("negativeOnly", negativeOnly)
                .append("positiveIsNegative", positiveIsNegative)
                .append("prefix", prefix)
                .append("searchOnly", searchOnly)
                .append("suffix", suffix)
                .append("units", units)
                .append("urlName", urlName)
                .append("exclusiveTo", exclusiveTo)
                .toString();
    }
}
