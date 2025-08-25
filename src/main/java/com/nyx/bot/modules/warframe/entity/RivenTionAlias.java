package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 紫卡词条别名
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"en", "cn"}))
@JsonView(Views.View.class)
public class RivenTionAlias extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //唯一自增ID
    Long id;
    //英文
    String en;
    //中文
    String cn;

    public RivenTionAlias() {
    }

    public RivenTionAlias(RivenTionAlias ra) {
        this.en = ra.en;
        this.cn = ra.cn;
    }
    @JsonIgnore
    public String getEquation() {
        return en + cn;
    }
}
