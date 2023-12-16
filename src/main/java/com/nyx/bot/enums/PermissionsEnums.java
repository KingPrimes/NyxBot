package com.nyx.bot.enums;

import lombok.Getter;

/**
 * 权限等级
 */
@Getter
public enum PermissionsEnums {
    //其他用户
    OTHER("其他用户"),
    //普通用户
    USER("普通用户"),
    //管理员用户
    ADMIN("管理员用户"),
    //超级管理员用户
    SUPER_ADMIN("超级管理员用户"),
    //后台用户
    MANAGE("后台用户"),
    ;
    private final String str;

    PermissionsEnums(String s) {
        this.str = s;
    }

}
