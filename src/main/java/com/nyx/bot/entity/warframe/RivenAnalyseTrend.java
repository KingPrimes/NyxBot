package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 紫卡分析参数
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@JsonView(Views.View.class)
public class RivenAnalyseTrend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    //Archwing枪械
    @JsonProperty("archwing")
    Double archwing;
    //近战
    @JsonProperty("melle")
    Double melle;
    //效果
    @JsonProperty("name")
    String name;
    //手枪
    @JsonProperty("pistol")
    Double pistol;
    //前缀
    @JsonProperty("prefix")
    String prefix;
    //步枪
    @JsonProperty("rifle")
    Double rifle;
    //霰弹枪
    @JsonProperty("shotgun")
    Double shotgun;
    //后缀
    @JsonProperty("suffix")
    String suffix;

}
