package com.nyx.bot.res.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Getter
public enum MissionTypeEnum {
    MT_ASSASSINATION("刺杀", 0),
    MT_EXTERMINATION("歼灭", 1),
    MT_SURVIVAL("生存", 2),
    MT_RESCUE("救援", 3),
    MT_SABOTAGE("破坏", 4),
    MT_CAPTURE("捕获", 5),
    MT_DEFAULT("未知", 6),
    MT_INTEL("间谍", 7),
    MT_DEFENSE("防御", 8),
    MT_MOBILE_DEFENSE("移动防御", 9),
    MT_PVP("武形秘仪",10),
    MT_SECTOR("黑暗地带",11),
    MT_TERRITORY("拦截", 13),
    MT_HIVE("清巢", 15),
    MT_RETRIEVAL("劫持", 14),
    MT_EXCAVATE("挖掘", 17),
    MT_SALVAGE("资源回收", 21),
    MT_ARENA("竞技场", 22),
    MT_PURSUIT("追击", 24),
    MT_ASSAULT("强袭", 26),
    MT_EVACUATION("叛逃", 27),
    MT_LANDSCAPE("自由漫步", 28),
    MT_ARTIFACT("中断", 33),
    MT_DISRUPTION("中断",32),
    MT_VOID_FLOOD("虚空洪流", 34),
    MT_VOID_CASCADE("虚空覆涌", 35),
    MT_VOID_ARMAGEDDON("虚空决战", 36),
    MT_ALCHEMY("元素转换", 38),
    MT_CAMBIRE("异化区",39),
    MT_LEGACYTE_HARVEST("传承种收割", 40),
    MT_SHRINE_DEFENSE("祈运坛防御", 41),
    MT_FACEOFF("对战", 42),
    MT_SKIRMISH("前哨战", 60),
    MT_VOLATILE("爆发", 61),
    MT_ORPHEUS("奧菲斯", 62),
    MT_ASCENSION("扬升",90),
    MT_RELAY("中继站", 100),
    ;
    private final String name;
    private final int order;

    MissionTypeEnum(String name, int order) {
        this.name = name;
        this.order = order;
    }

    public static List<MissionTypeEnum> getOrderedValues() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(MissionTypeEnum::getOrder))
                .toList();
    }
}
