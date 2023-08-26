package com.nyx.bot.entity.sys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Web 系统用户
 */
@Data
@Entity
@Table
public class SysUser {

    @Id
    Long userId;
    String userName;
    String password;
}
