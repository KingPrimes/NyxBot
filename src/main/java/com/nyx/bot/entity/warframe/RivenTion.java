package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    Long ids;

    @JsonProperty("effect")
    String effect;

    @JsonProperty("`group`")
    @Column(name = "`group`")
    String group;

    @JsonProperty("negative_only")
    Double negativeOnly;

    @JsonProperty("negative_is_negative")
    Double positiveIsNegative;

    @JsonProperty("prefix")
    String prefix;

    @JsonProperty("search_only")
    Double searchOnly;

    @JsonProperty("suffix")
    String suffix;

    @JsonProperty("units")
    String units;

    @JsonProperty("url_name")
    String urlName;

    @JsonProperty("exclusive_to")
    String exclusiveTo;
}
