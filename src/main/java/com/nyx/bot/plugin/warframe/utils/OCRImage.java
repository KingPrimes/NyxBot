package com.nyx.bot.plugin.warframe.utils;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.utils.ocr.OcrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OCRImage {

    /**
     * 识别紫卡文字
     *
     * @param event 消息事件
     */
    public static List<List<String>> ocrImage(AnyMessageEvent event) {
        List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
        List<List<String>> images = new ArrayList<>();
        try {
            for (String s : msgImgUrlList) {
                images.add(OcrUtil.ocr(s));
            }
        } catch (Exception e) {
            log.error("识别图片报错：{}", e.getMessage());
        }
        return images;

    }

}
