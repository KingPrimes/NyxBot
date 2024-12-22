package com.nyx.bot.entity.sys;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
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
