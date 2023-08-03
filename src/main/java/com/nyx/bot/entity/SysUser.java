package com.nyx.bot.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userId;
    String userName;
    String userType;
    String password;
    //盐加密
    String salt;
    //状态
    String status;
    String delFlag;

}
