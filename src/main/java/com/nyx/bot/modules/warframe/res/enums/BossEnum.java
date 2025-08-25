package com.nyx.bot.modules.warframe.res.enums;

import lombok.Getter;

@Getter
public enum BossEnum {
    SORTIE_BOSS_HYENA("鬣狗群", "Corpus"),
    SORTIE_BOSS_KELA("Kela De Thaym", "Grineer"),
    SORTIE_BOSS_VOR("Vor 上尉", "Grineer"),
    SORTIE_BOSS_RUK("Sargas Ruk 将军", "Grineer"),
    SORTIE_BOSS_HEK("Vay Hek 委员", "Grineer"),
    SORTIE_BOSS_KRIL("Lech Kril 中尉", "Grineer"),
    SORTIE_BOSS_TYL("Tyl Regor", "Grineer"),
    SORTIE_BOSS_JACKAL("豺狼", "Corpus"),
    SORTIE_BOSS_ALAD("Alad V", "Corpus"),
    SORTIE_BOSS_AMBULAS("Ambulas", "Corpus"),
    SORTIE_BOSS_NEF("Nef Anyo", "Corpus"),
    SORTIE_BOSS_RAPTOR("猛禽", "Corpus"),
    SORTIE_BOSS_PHORID("Phorid", "Infestation"),
    SORTIE_BOSS_LEPHANTIS("Lephantis", "Infestation"),
    SORTIE_BOSS_INFALAD("异融者 Alad V", "Infestation"),
    SORTIE_BOSS_CORRUPTED_VOR("堕落 Vor", "堕落者"),
    SORTIE_BOSS_BOREAL("诡文枭主", "合一众"),
    SORTIE_BOSS_AMAR("欺谋狼主", "合一众"),
    SORTIE_BOSS_NIRA("混沌蛇主", "合一众"),
    SORTIE_BOSS_PAAZUL("帕祖", "合一众");

    private final String name;
    private final String faction;

    BossEnum(String name, String faction) {
        this.name = name;
        this.faction = faction;
    }
}
