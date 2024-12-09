package com.nyx.bot.utils.onebot;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.common.utils.Keyboard;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.model.ArrayMsg;
import org.eclipse.jgit.util.Base64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Msg {

    private final List<ArrayMsg> builder = new ArrayList<>();

    /**
     * 消息构建
     *
     * @return {@link Msg}
     */
    public static Msg builder() {
        return new Msg();
    }

    /**
     * 文本内容
     *
     * @param text 内容
     * @return {@link Msg}
     */
    public Msg text(String text) {
        builder.add(getJsonData("text", m -> m.put("text", text)));
        return this;
    }

    /**
     * 图片
     * 支持本地图片、网络图片、Base64 详见 <a href="https://misakatat.github.io/shiro-docs/advanced.html#">消息构建工具</a>
     *
     * @param img 图片
     * @return {@link Msg}
     */
    public Msg img(String img) {
        builder.add(getJsonData("image", m -> m.put("file", ShiroUtils.escape(img))));
        return this;
    }

    /**
     * 图片
     *
     * @param media {@link OneBotMedia}
     * @return {@link Msg}
     */
    public Msg img(OneBotMedia media) {
        builder.add(getJsonData("image", media::escape));
        return this;
    }

    /**
     * 短视频
     *
     * @param video 视频地址, 支持 http 和 file 发送
     * @param cover 视频封面, 支持 http, file 和 base64 发送, 格式必须为 jpg
     * @return {@link Msg}
     */
    public Msg video(String video, String cover) {
        builder.add(getJsonData("video", m -> {
            m.put("file", ShiroUtils.escape(video));
            m.put("cover", ShiroUtils.escape(cover));
        }));
        return this;
    }


    /**
     * 闪照
     *
     * @param img 图片
     * @return {@link Msg}
     */
    public Msg flashImg(String img) {
        builder.add(getJsonData("image", m -> {
            m.put("flash", "flash");
            m.put("file", ShiroUtils.escape(img));
        }));
        return this;
    }

    /**
     * QQ 表情
     * <a href="https://github.com/kyubotics/coolq-http-api/wiki/%E8%A1%A8%E6%83%85-CQ-%E7%A0%81-ID-%E8%A1%A8">对照表</a>
     *
     * @param id QQ 表情 ID
     * @return {@link Msg}
     */
    public Msg face(int id) {
        builder.add(getJsonData("face", m -> m.put("id", String.valueOf(id))));
        return this;
    }

    /**
     * 语音
     *
     * @param media {@link OneBotMedia}
     * @return {@link Msg}
     */
    public Msg voice(OneBotMedia media) {
        builder.add(getJsonData("record", media::escape));
        return this;
    }

    /**
     * 语音
     *
     * @param voice 语音, 支持本地文件和 URL
     * @return {@link Msg}
     */
    public Msg voice(String voice) {
        builder.add(getJsonData("record", m -> m.put("file", ShiroUtils.escape(voice))));
        return this;
    }

    /**
     * at 某人
     *
     * @param userId at 的 QQ 号, all 表示全体成员
     * @return {@link Msg}
     */
    public Msg at(long userId) {
        builder.add(getJsonData("at", m -> m.put("qq", String.valueOf(userId))));
        return this;
    }

    /**
     * at 全体成员
     *
     * @return {@link Msg}
     */
    public Msg atAll() {
        builder.add(getJsonData("at", m -> m.put("qq", "all")));
        return this;
    }

    /**
     * 戳一戳
     *
     * @param userId 需要戳的成员
     * @return {@link Msg}
     */
    public Msg poke(long userId) {
        builder.add(getJsonData("poke", m -> m.put("qq", String.valueOf(userId))));
        return this;
    }

    /**
     * 回复
     *
     * @param msgId 回复时所引用的消息 id, 必须为本群消息.
     * @return {@link Msg}
     */
    public Msg reply(int msgId) {
        builder.add(getJsonData("reply", m -> m.put("id", String.valueOf(msgId))));
        return this;
    }

    /**
     * 回复-频道
     *
     * @param msgId 回复时所引用的消息 id, 必须为本频道消息.
     * @return {@link Msg}
     */
    public Msg reply(String msgId) {
        builder.add(getJsonData("reply", m -> m.put("id", msgId)));
        return this;
    }

    /**
     * 礼物
     * 仅支持免费礼物, 发送群礼物消息 无法撤回, 返回的 message id 恒定为 0
     *
     * @param userId 接收礼物的成员
     * @param giftId 礼物的类型
     * @return {@link Msg}
     */
    public Msg gift(long userId, int giftId) {
        builder.add(getJsonData("gift", m -> {
            m.put("qq", String.valueOf(userId));
            m.put("id", String.valueOf(giftId));
        }));
        return this;
    }

    /**
     * 文本转语音
     * 通过腾讯的 TTS 接口, 采用的音源与登录账号的性别有关
     *
     * @param text 内容
     * @return {@link Msg}
     */
    public Msg tts(String text) {
        builder.add(getJsonData("tts", m -> m.put("text", text)));
        return this;
    }

    /**
     * XML 消息
     *
     * @param data xml内容, xml 中的 value 部分, 记得实体化处理
     * @return {@link Msg}
     */
    public Msg xml(String data) {
        builder.add(getJsonData("xml", m -> m.put("data", data)));
        return this;
    }

    /**
     * XML 消息
     *
     * @param data  xml 内容, xml 中的 value部分, 记得实体化处理
     * @param resId 可以不填
     * @return {@link Msg}
     */
    public Msg xml(String data, int resId) {
        builder.add(getJsonData("xml", m -> {
            m.put("data", String.valueOf(data));
            m.put("resid", String.valueOf(resId));
        }));
        return this;
    }

    /**
     * JSON 消息
     *
     * @param data json 内容, json 的所有字符串记得实体化处理
     * @return {@link Msg}
     */
    public Msg json(String data) {
        builder.add(getJsonData("json", m -> m.put("data", data)));
        return this;
    }

    /**
     * JSON 消息
     *
     * @param data  json 内容, json 的所有字符串记得实体化处理
     * @param resId 默认不填为 0, 走小程序通道, 填了走富文本通道发送
     * @return {@link Msg}
     */
    public Msg json(String data, int resId) {
        builder.add(getJsonData("json", m -> {
            m.put("data", String.valueOf(data));
            m.put("resid", String.valueOf(resId));
        }));
        return this;
    }

    /**
     * 一种 xml 的图片消息
     * xml 接口的消息都存在风控风险, 请自行兼容发送失败后的处理 ( 可以失败后走普通图片模式 )
     *
     * @param file 和 image 的 file 字段对齐, 支持也是一样的
     * @return {@link Msg}
     */
    public Msg cardImage(String file) {
        builder.add(getJsonData("cardimage", m -> m.put("file", String.valueOf(file))));
        return this;
    }

    /**
     * 一种 xml 的图片消息
     * xml 接口的消息都存在风控风险, 请自行兼容发送失败后的处理 ( 可以失败后走普通图片模式 )
     *
     * @param file      和 image 的 file 字段对齐, 支持也是一样的
     * @param minWidth  默认不填为 400, 最小 width
     * @param minHeight 默认不填为 400, 最小 height
     * @param maxWidth  默认不填为 500, 最大 width
     * @param maxHeight 默认不填为 1000, 最大 height
     * @param source    分享来源的名称, 可以留空
     * @param icon      分享来源的 icon 图标 url, 可以留空
     * @return {@link Msg}
     */
    public Msg cardImage(String file, long minWidth, long minHeight, long maxWidth, long maxHeight, String source, String icon) {
        builder.add(getJsonData("cardimage", m -> {
            m.put("file", ShiroUtils.escape(file));
            m.put("minwidth", String.valueOf(minWidth));
            m.put("minheight", String.valueOf(minHeight));
            m.put("maxwidth", String.valueOf(maxWidth));
            m.put("maxheight", String.valueOf(maxHeight));
            m.put("source", ShiroUtils.escape(source));
            m.put("icon", ShiroUtils.escape(icon));
        }));
        return this;
    }

    /**
     * 音乐分享
     *
     * @param type qq 163 xm (分别表示使用 QQ 音乐、网易云音乐、虾米音乐)
     * @param id   歌曲 ID
     * @return {@link Msg}
     */
    @SuppressWarnings({"java:S1192"})
    public Msg music(String type, long id) {
        builder.add(getJsonData("music", m -> {
            m.put("type", String.valueOf(type));
            m.put("id", String.valueOf(id));
        }));
        return this;
    }

    /**
     * 音乐自定义分享
     *
     * @param url     点击后跳转目标 URL
     * @param audio   音乐 URL
     * @param title   标题
     * @param content 发送时可选，内容描述
     * @param image   发送时可选，图片 URL
     * @return {@link Msg}
     */
    @SuppressWarnings({"java:S1192"})
    public Msg customMusic(String url, String audio, String title, String content, String image) {
        builder.add(getJsonData("music", m -> {
            m.put("type", "custom");
            m.put("url", ShiroUtils.escape(url));
            m.put("audio", ShiroUtils.escape(audio));
            m.put("title", ShiroUtils.escape(title));
            m.put("content", ShiroUtils.escape(content));
            m.put("image", ShiroUtils.escape(image));
        }));
        return this;
    }

    /**
     * 音乐自定义分享
     *
     * @param url   点击后跳转目标 URL
     * @param audio 音乐 URL
     * @param title 标题
     * @return {@link Msg}
     */
    @SuppressWarnings({"java:S1192"})
    public Msg customMusic(String url, String audio, String title) {
        builder.add(getJsonData("music", m -> {
            m.put("type", "custom");
            m.put("url", ShiroUtils.escape(url));
            m.put("audio", ShiroUtils.escape(audio));
            m.put("title", ShiroUtils.escape(title));
        }));
        return this;
    }

    /**
     * 发送猜拳消息
     *
     * @param value 0石头 1剪刀 2布
     * @return {@link Msg}
     */
    public Msg rps(int value) {
        builder.add(getJsonData("rps", m -> m.put("value", String.valueOf(value))));
        return this;
    }

    /**
     * 发送Markdown文档消息
     *
     * @param content 文档内容
     * @return {@link Msg}
     */
    public Msg markdown(String content) {
        builder.add(getJsonData("markdown", m -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("content", content);
            m.put("content", JSON.toJSONString(map));
        }));
        return this;
    }


    /**
     * 发送Markdown文档 按钮
     * <pre>{@code
     *     Keyboard keyboard = Keyboard.Builder()
     *     .addRow()
     *     .addButton(Keyboard.TextButtonBuilder()
     *          .label("+1")
     *          .data("md2")
     *          .build()
     *          )
     *     .build();
     *     //完整调用
     *     Msg.builder().markdown("123").keyboard(keyboard).buildList();
     * }</pre>
     */
    public Msg keyboard(Keyboard keyboard) {
        builder.add(getJsonData("keyboard", m -> m.put("keyboard", JSON.toJSONString(keyboard))));
        return this;
    }

    public List<ArrayMsg> build() {
        return builder;
    }

    private ArrayMsg getJsonData(String type, Consumer<Map<String, String>> consumer) {
        HashMap<String, String> data = new HashMap<>();
        consumer.accept(data);
        return new ArrayMsg().setRawType(type).setData(data);
    }

    /**
     * 图片
     *
     * @param b Base64 byte[]
     * @return {@link Msg}
     */
    public Msg imgBase64(byte[] b) {
        return img(ShiroUtils.escape("base64://" + Base64.encodeBytes(b)));
    }

}
