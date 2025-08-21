package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum SyndicateEnum {
    ArbitersSyndicate("均衡仲裁者"),
    CephalonSudaSyndicate("中枢苏达"),
    NewLokaSyndicate("新世间"),
    PerrinSyndicate("佩兰数列"),
    SteelMeridianSyndicate("钢铁防线"),
    RedVeilSyndicate("血色面纱"),
    CetusSyndicate("Ostron"),
    QuillsSyndicate("夜羽"),
    AssassinsSyndicate("刺客"),
    EventSyndicate("集团"),
    SolarisSyndicate("索拉里斯联盟"),
    VoxSyndicate("索拉里斯之声"),
    VentKidsSyndicate("通风小子"),
    EntratiSyndicate("英择谛"),
    EntratiLabSyndicate("科维兽"),
    HexSyndicate("The Hex"),
    NecraloidSyndicate("殁世械灵"),
    KahlSyndicate("卡尔驻军"),
    ZarimanSyndicate("坚守者"),
    RadioLegionSyndicate("土星六号之狼"),
    RadioLegion2Syndicate("使徒"),
    RadioLegion3Syndicate("玻璃匠"),
    RadioLegionIntermissionSyndicate("Intermission"),
    RadioLegionIntermission2Syndicate("Intermission II"),
    RadioLegionIntermission3Syndicate("Intermission III"),
    RadioLegionIntermission4Syndicate("Nora 的精选"),
    RadioLegionIntermission5Syndicate("Nora 的混选 Vol. 1"),
    RadioLegionIntermission6Syndicate("Nora 的混选 Vol. 2"),
    RadioLegionIntermission7Syndicate("RadioLegionIntermission7Syndicate"),
    RadioLegionIntermission8Syndicate("RadioLegionIntermission8Syndicate"),
    RadioLegionIntermission9Syndicate("RadioLegionIntermission9Syndicate"),
    RadioLegionIntermission10Syndicate("RadioLegionIntermission10Syndicate"),
    RadioLegionIntermission11Syndicate("RadioLegionIntermission11Syndicate"),
    RadioLegionIntermission12Syndicate("RadioLegionIntermission12Syndicate"),
    RadioLegionIntermission13Syndicate("RadioLegionIntermission13Syndicate"),

    ;

    private final String name;

    SyndicateEnum(String name) {
        this.name = name;
    }
}
