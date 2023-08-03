package com.nyx.bot.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Web 系统用户
 */
@Data
@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    Long userId;
    String userName;
    String password;
}
