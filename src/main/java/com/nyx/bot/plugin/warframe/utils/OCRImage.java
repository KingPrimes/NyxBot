package com.nyx.bot.plugin.warframe.utils;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.entity.warframe.RivenAnaiyseTrend;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.utils.MatchUtil;
import com.nyx.bot.utils.ocr.PaddlePaddleOCRV4;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OCRImage {

    public static void ocrImage(Bot bot, AnyMessageEvent event, String str, Codes code) {
        PaddlePaddleOCRV4 instance = PaddlePaddleOCRV4.INSTANCE;
        List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
        List<List<String>> images = new ArrayList<>();
        try {
            for (String s : msgImgUrlList) {
                images.add(instance.ocr(s));
            }
            List<RivenAnaiyseTrend> riven = getRiven(images);
            riven.forEach(System.out::println);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private static List<RivenAnaiyseTrend> getRiven(List<List<String>> images) {
        List<RivenAnaiyseTrend> trends = new ArrayList<>();
        for (List<String> image : images) {
            RivenAnaiyseTrend trend = new RivenAnaiyseTrend();
            for (String s : image) {
                if (s.length() < 3) {
                    continue;
                }
                if (MatchUtil.isWeaponsName(s)) {
                    trend.setWeaponsName(MatchUtil.getChines(s));
                    if (MatchUtil.isRivenNameEx(s)) {
                        trend.setRivenName(MatchUtil.getRivenNameE(s));
                    }
                }
                if (MatchUtil.isRivenNameEx(s)) {
                    if (trend.getRivenName() == null) {
                        trend.setRivenName(MatchUtil.getRivenNameE(s));
                    } else {
                        if (!trend.getRivenName().equals(MatchUtil.getRivenNameE(s))) {
                            trend.setRivenName(trend.getRivenName() + MatchUtil.getRivenNameE(s));
                        }
                    }
                }
                if (MatchUtil.isAttribute(s)) {
                    if (s.contains("入")) {
                        s = s.replace("入", "");
                    }
                    RivenAnaiyseTrend.Attribute attribute = new RivenAnaiyseTrend.Attribute();
                    if (s.contains("射速")) {
                        attribute.setName(MatchUtil.getAttribetName(s) + " 效果加倍）");
                    } else {
                        attribute.setName(MatchUtil.getAttribetName(s));
                    }
                    attribute.setAttribute(MatchUtil.getAttributeNum(s));
                    attribute.setNag(attribute.getAttribute() < 0);
                    trend.add(attribute);
                }
            }
            trends.add(trend);
        }
        return trends;
    }
}
