package com.nyx.bot.plugin.expression.code;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.plugin.expression.utils.Expressions;
import com.nyx.bot.utils.image.DrawingImageUtils;
import com.nyx.bot.utils.onebot.Msg;
import com.nyx.bot.utils.onebot.PrivateUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class ExpressionCode {

    public static void expression(Bot bot, AnyMessageEvent event, Codes code) {
        switch (code) {
            case EXPRESSION_CAPO_DING -> {
                capoDing(bot, event);
            }
            case EXPRESSION_CAPO_T -> {
                capoT(bot, event);
            }
            case EXPRESSION_GRAY_IMAGE -> {
                gray(bot, event);
            }
            case EXPRESSION_BARBARIZATION -> {
                bar(bot, event);
            }
            case EXPRESSION_EMAIL_FUNNY -> {
                emailFunny(bot, event);
            }
            case EXPRESSION_MIRROR_IMAGE -> {
                mirror(bot, event);
            }
            case EXPRESSION_SPIRITUAL_PILLARS -> {
                spiritualPillars(bot, event);
            }
        }
    }

    static BufferedImage getImage(long userId, int size) throws IOException {
        return ImageIO.read(new URL(PrivateUtils.getUserQzone(userId, size)));
    }

    static BufferedImage getImage(long userId) throws IOException {

        return ImageIO.read(new URL(PrivateUtils.getUserQzone(userId)));
    }

    static BufferedImage getImage(String url) throws IOException {
        return ImageIO.read(new URL(url));
    }

    static void capoDing(Bot bot, AnyMessageEvent event) {
        try {
            List<Long> atList = ShiroUtils.getAtList(event.getArrayMsg());
            Msg msg = Msg.builder();
            if (atList.isEmpty()) {
                msg.imgBase64(Objects.requireNonNull(Expressions.capooDing(getImage(event.getUserId()), 0)).toByteArray());
            } else {
                for (Long l : atList) {
                    msg.imgBase64(Objects.requireNonNull(Expressions.capooDing(getImage(l), 0)).toByteArray());
                }
            }

            bot.sendMsg(event, msg.build(), false);
        } catch (Exception ignored) {

        }
    }

    static void capoT(Bot bot, AnyMessageEvent event) {
        try {
            List<Long> atList = ShiroUtils.getAtList(event.getArrayMsg());
            Msg msg = Msg.builder();
            if (atList.isEmpty()) {
                msg.imgBase64(Objects.requireNonNull(Expressions.capooT(getImage(event.getUserId()), 0)).toByteArray());
            } else {
                for (Long l : atList) {
                    msg.imgBase64(Objects.requireNonNull(Expressions.capooT(getImage(l), 0)).toByteArray());
                }
            }

            bot.sendMsg(event, msg.build(), false);
        } catch (Exception ignored) {

        }
    }

    static void emailFunny(Bot bot, AnyMessageEvent event) {
        try {
            List<Long> atList = ShiroUtils.getAtList(event.getArrayMsg());
            Msg msg = Msg.builder();
            if (atList.isEmpty()) {
                msg.imgBase64(Objects.requireNonNull(Expressions.emailFunny(getImage(event.getUserId()), 0)).toByteArray());
            } else {
                for (Long l : atList) {
                    msg.imgBase64(Objects.requireNonNull(Expressions.emailFunny(getImage(l), 0)).toByteArray());
                }
            }

            bot.sendMsg(event, msg.build(), false);
        } catch (Exception ignored) {

        }
    }

    static void spiritualPillars(Bot bot, AnyMessageEvent event) {
        try {
            List<Long> atList = ShiroUtils.getAtList(event.getArrayMsg());
            Msg msg = Msg.builder();
            if (atList.isEmpty()) {
                msg.imgBase64(Objects.requireNonNull(Expressions.spiritualPillars(getImage(event.getUserId(), 640))).toByteArray());
            } else {
                for (Long l : atList) {
                    msg.imgBase64(Objects.requireNonNull(Expressions.spiritualPillars(getImage(l, 640))).toByteArray());
                }
            }

            bot.sendMsg(event, msg.build(), false);
        } catch (Exception ignored) {

        }
    }

    static void gray(Bot bot, AnyMessageEvent event) {
        try {
            List<Long> atList = ShiroUtils.getAtList(event.getArrayMsg());
            List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
            Msg msg = Msg.builder();
            if (!msgImgUrlList.isEmpty()) {
                msg.imgBase64(DrawingImageUtils.gray(getImage(msgImgUrlList.get(0))).toByteArray());
                bot.sendMsg(event, msg.build(), false);
                return;
            }
            if (atList.isEmpty()) {
                msg.imgBase64(Objects.requireNonNull(DrawingImageUtils.gray(getImage(event.getUserId(), 640))).toByteArray());
            } else {
                for (Long l : atList) {
                    msg.imgBase64(Objects.requireNonNull(DrawingImageUtils.gray(getImage(l, 640))).toByteArray());
                }
            }
            bot.sendMsg(event, msg.build(), false);
        } catch (Exception ignored) {

        }
    }

    static void bar(Bot bot, AnyMessageEvent event) {
        try {
            List<Long> atList = ShiroUtils.getAtList(event.getArrayMsg());
            List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
            Msg msg = Msg.builder();
            if (!msgImgUrlList.isEmpty()) {
                msg.imgBase64(DrawingImageUtils.barbar(getImage(msgImgUrlList.get(0))).toByteArray());
                bot.sendMsg(event, msg.build(), false);
                return;
            }
            if (atList.isEmpty()) {
                msg.imgBase64(Objects.requireNonNull(DrawingImageUtils.barbar(getImage(event.getUserId(), 640))).toByteArray());
            } else {
                for (Long l : atList) {
                    msg.imgBase64(Objects.requireNonNull(DrawingImageUtils.barbar(getImage(l, 640))).toByteArray());
                }
            }
            bot.sendMsg(event, msg.build(), false);
        } catch (Exception ignored) {

        }
    }

    static void mirror(Bot bot, AnyMessageEvent event) {
        try {
            List<Long> atList = ShiroUtils.getAtList(event.getArrayMsg());
            List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
            Msg msg = Msg.builder();
            if (!msgImgUrlList.isEmpty()) {
                msg.imgBase64(DrawingImageUtils.mirror(getImage(msgImgUrlList.get(0))).toByteArray());
                bot.sendMsg(event, msg.build(), false);
                return;
            }
            if (atList.isEmpty()) {
                msg.imgBase64(Objects.requireNonNull(DrawingImageUtils.mirror(getImage(event.getUserId(), 640))).toByteArray());
            } else {
                for (Long l : atList) {
                    msg.imgBase64(Objects.requireNonNull(DrawingImageUtils.mirror(getImage(l, 640))).toByteArray());
                }
            }
            bot.sendMsg(event, msg.build(), false);
        } catch (Exception ignored) {

        }
    }

}
