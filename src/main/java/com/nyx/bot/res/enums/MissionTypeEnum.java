package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum MissionTypeEnum {
    MT_DEFAULT("未知"),
    MT_ARENA("竞技场"),
    MT_ARTIFACT("中断"),
    MT_ASSAULT("强袭"),
    MT_ASSASSINATION("刺杀"),
    MT_CAPTURE("捕获"),
    MT_DEFENSE("防御"),
    MT_DISRUPTION("中断"),
    MT_EVACUATION("叛逃"),
    MT_EXCAVATE("挖掘"),
    MT_EXTERMINATION("歼灭"),
    MT_HIVE("清巢"),
    MT_INTEL("间谍"),
    MT_LANDSCAPE("自由漫步"),
    MT_MOBILE_DEFENSE("移动防御"),
    MT_PVP("武形秘仪"),
    MT_RESCUE("救援"),
    MT_RETRIEVAL("劫持"),
    MT_SABOTAGE("破坏"),
    MT_SECTOR("黑暗地带"),
    MT_SURVIVAL("生存"),
    MT_VOID_CASCADE("虚空覆涌"),
    MT_ASCENSION("扬升")
    ;
    private final String name;

    MissionTypeEnum(String name) {
        this.name = name;
    }
}
