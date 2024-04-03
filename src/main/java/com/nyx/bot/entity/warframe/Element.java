package com.nyx.bot.entity.warframe;

import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"en", "cn"}))
public class Element extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String en;

    String cn;

}
