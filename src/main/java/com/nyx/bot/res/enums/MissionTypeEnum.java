package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum MissionTypeEnum {
    MT_ASSASSINATION("刺杀"),
    MT_EXTERMINATION("歼灭"),
    MT_SURVIVAL("生存"),
    MT_RESCUE("救援"),
    MT_SABOTAGE("破坏"),
    MT_CAPTURE("捕获"),
    MT_DEFAULT("未知"),
    MT_INTEL("间谍"),
    MT_DEFENSE("防御"),
    MT_MOBILE_DEFENSE("移动防御"),
    MT_ARENA("竞技场"),
    MT_ARTIFACT("中断"),
    MT_ASSAULT("强袭"),
    MT_DISRUPTION("中断"),
    MT_EVACUATION("叛逃"),
    MT_EXCAVATE("挖掘"),
    MT_HIVE("清巢"),
    MT_LANDSCAPE("自由漫步"),
    MT_PVP("武形秘仪"),
    MT_RETRIEVAL("劫持"),
    MT_SECTOR("黑暗地带"),
    MT_VOID_CASCADE("虚空覆涌"),
    MT_VOID_FLOOD("虚空洪流"),
    MT_ALCHEMY("元素转换"),
    MT_CAMBIRE("异化区"),
    MT_VOID_ARMAGEDDON("虚空决战"),
    MT_ASCENSION("扬升"),
    MT_TERRITORY("拦截"),
    MT_SALVAGE("资源回收"),
    MT_PURSUIT("追击"),
    MT_LEGACYTE_HARVEST("传承种收割"),
    MT_SHRINE_DEFENSE("祈运坛防御"),
    MT_FACEOFF("对战")
    ;
    private final String name;

    MissionTypeEnum(String name) {
        this.name = name;
    }
}
