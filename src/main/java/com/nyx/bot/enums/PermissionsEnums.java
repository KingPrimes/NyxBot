package com.nyx.bot.enums;

import lombok.Getter;

/**
 * 权限等级
 */
@Getter
public enum PermissionsEnums {
    //其他用户
    OTHER("其他用户", -1),
    //超级管理员用户
    SUPER_ADMIN("超级管理员用户", 0),
    //管理员用户
    ADMIN("管理员用户", 1),
    //普通用户
    USER("普通用户", 2),
    //后台用户
    MANAGE("后台用户", 3),
    ;
    private final String str;
    private final int i;

    PermissionsEnums(String s, int i) {
        this.str = s;
        this.i = i;
    }

}
