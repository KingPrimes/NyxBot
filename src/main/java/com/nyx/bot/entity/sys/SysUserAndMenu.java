package com.nyx.bot.entity.sys;

import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class SysUserAndMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;

    Long menuId;

}
