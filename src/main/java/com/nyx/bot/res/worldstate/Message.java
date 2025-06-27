package com.nyx.bot.res.worldstate;

import lombok.Data;

@Data
public class Message {
    private LanguageCode languageCode;
    private String message;

    public enum LanguageCode {
        EN,
        ZH,
        KO,
        RU,
        TR,
        ES,
        FR,
        DE,
        IT,
        JP,
        PL,
        PT,
        VI,
        ID,
        TH,
        AR,
        FA,
        HE,
        HY,
        KA,
        KR,
        MS,
        NO,
        SQ,
        SW,
        TL,
        UK,
        UZ,
        ZH_TW
    }
}
