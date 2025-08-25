package com.nyx.bot.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

/**
 * Web 系统用户
 */
@Data
@Entity
@Table
public class SysUser {

    @Id
    @JsonView(Views.View.class)
    Long userId;
    @JsonView(Views.View.class)
    String userName;

    String password;

    @Transient
    String token;
}
