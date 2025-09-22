package com.nyx.bot.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.utils.ocr.OcrUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestOcr {

    @Test
    public void ocr(){
        List<String> paths = List.of(
                "D:\\Demos\\NyxBot\\data\\riven\\bingsong.png",
                "D:\\Demos\\NyxBot\\data\\riven\\chengjiezhe.png",
                "D:\\Demos\\NyxBot\\data\\riven\\liangziqiege.jpg",
                "D:\\Demos\\NyxBot\\data\\riven\\zhanren.png"
        );
        for (String path : paths) {
            List<String> strings = OcrUtil.ocrPath(path);
            System.out.println("识别文字：" + JSON.toJSONString(strings));
            System.out.println("-----------------------------------------");
        }
    }
}
