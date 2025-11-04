package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import io.github.kingprimes.model.WorldState;


import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Constant {

    /**
     * 绘制图片保存路径</br>
     * %s 为图片名称 </br>
     * 使用 DRAW_PATH.formatted("图片名称") 获取图片路径
     */
    public static final String DRAW_PATH = "./draw/%s";

    public static final String PNG = "png";

    public static final String WORLD_STATUS_PATH = "./data/state-2025-11-4.json";

    static FileInputStream state;

    static {
        try {
            state = new FileInputStream(WORLD_STATUS_PATH);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static final WorldState WORLD_STATE = JSON.parseObject(state, WorldState.class);

}
