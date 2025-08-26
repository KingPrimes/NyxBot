package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
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

    public RivenAnalyseTrend() {
    }

    public RivenAnalyseTrend(RivenAnalyseTrend rat) {
        this.archwing = rat.archwing;
        this.melle = rat.melle;
        this.name = rat.name;
        this.pistol = rat.pistol;
        this.prefix = rat.prefix;
        this.rifle = rat.rifle;
        this.shotgun = rat.shotgun;
        this.suffix = rat.suffix;
    }
    @JsonIgnore
    public String getEquation() {
        return archwing + melle + name + pistol + prefix + rifle + shotgun + suffix;
    }
}
