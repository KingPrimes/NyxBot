package com.nyx.bot.enums;

import lombok.Getter;

@Getter
public enum ServicesEnums {
    ICQQ_ONEBOTS("ICQQ"),
    GO_CQHTTP("GoCqHttp"),
    BILIBILI("B站"),
    WARFRAME("Warframe"),
    CHAT_GPT("Chat-Gpt"),
    EPIC_GAMES("Epic"),
    STABLE_DIFFUSION("Stable diffusion"),
    YI_YAN("文心一言"),
    MUSIC("点歌"),
    LOOK_IMAGE("鉴图"),
    DRAW_EMOJIS("绘制表情包"),
    ACG_IMAGE("色图"),
    ;
    final String str;

    ServicesEnums(String s) {
        str = s;
    }
}
