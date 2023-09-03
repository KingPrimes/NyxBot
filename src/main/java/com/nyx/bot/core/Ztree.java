package com.nyx.bot.core;

import lombok.Data;

@Data
public class Ztree {
    /**
     * 节点ID
     */
    Long id;

    /**
     * 节点父ID
     */
    Long pId;

    /**
     * 节点名称
     */
    String name;

    /**
     * 节点标题
     */
    String title;

    /**
     * 是否勾选
     */
    boolean checked = false;

    /**
     * 是否展开
     */
    boolean open = false;

    /**
     * 是否能勾选
     */
    boolean nocheck = false;
}
