package com.nyx.bot.entity.warframe;

import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 紫卡词条别名
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"en", "cn"}))
public class RivenTionAlias extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String en;

    String cn;
}
