package com.nyx.bot.enums;

import lombok.Getter;

@Getter
public enum BusinessType {
    /**
     * 其它
     */
    OTHER("其它"),

    /**
     * 查询
     */
    SELECT("查询"),

    /**
     * 新增
     */
    INSERT("新增"),

    /**
     * 修改
     */
    UPDATE("修改"),

    /**
     * 删除
     */
    DELETE("删除"),

    /**
     * 导出
     */
    EXPORT("导出"),

    /**
     * 导入
     */
    IMPORT("导入"),

    /**
     * 清空
     */
    CLEAN("清空"),
    /**
     * 生成图片
     */
    IMAGE("生成图片"),

    PLUGIN("插件"),
    ;
    final String type;

    BusinessType(String type) {
        this.type = type;
    }

}
