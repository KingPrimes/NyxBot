package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Message {
    @JsonProperty("LanguageCode")
    private LanguageCode languageCode;
    @JsonProperty("Message")
    private String message;

    public enum LanguageCode {
        en,
        zh,
        ko,
        ru,
        tr,
        es,
        fr,
        de,
        it,
        jp,
        pl,
        pt,
        vi,
        id,
        th,
        ar,
        fa,
        he,
        hy,
        ka,
        kr,
        ms,
        no,
        sq,
        sw,
        tl,
        uk,
        uz,
        zh_tw
    }
}
