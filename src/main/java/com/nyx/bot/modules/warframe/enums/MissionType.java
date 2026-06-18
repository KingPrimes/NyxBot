package com.nyx.bot.modules.warframe.enums;

import lombok.Getter;

/**
 * 订阅任务类型枚举
 *
 * @author KingPrimes
 */
@Getter
public enum MissionType {

    MT_EXTERMINATION("歼灭"),
    MT_SURVIVAL("生存"),
    MT_RESCUE("救援"),
    MT_SABOTAGE("破坏"),
    MT_CAPTURE("捕获"),
    MT_INTEL("间谍"),
    MT_DEFENSE("防御"),
    MT_MOBILE_DEFENSE("移动防御"),
    MT_TERRITORY("拦截"),
    MT_HIVE("清巢"),
    MT_RETRIEVAL("劫持"),
    MT_EXCAVATE("挖掘"),
    MT_SALVAGE("资源回收"),
    MT_PURSUIT("追击"),
    MT_ASSAULT("强袭"),
    MT_EVACUATION("叛逃"),
    MT_DISRUPTION("中断"),
    MT_VOID_FLOOD("虚空洪流"),
    MT_VOID_CASCADE("虚空覆涌"),
    MT_VOID_ARMAGEDDON("虚空决战"),
    MT_ALCHEMY("元素转换"),
    MT_CAMBIRE("异化区"),
    MT_SKIRMISH("前哨战"),
    MT_VOLATILE("爆发"),
    MT_ORPHEUS("奧菲斯"),
    MT_ASCENSION("扬升"),
    MT_CORRUPTION("虚空腐蚀"),
    ;

    private final String name;

    MissionType(String name) {
        this.name = name;
    }
}
