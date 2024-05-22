package com.nyx.bot.enums;

public enum WarframeMissionTypeEnum {
    Assassination("刺杀"),
    Assault("强袭"),
    Capture("捕获"),
    Defection("叛逃"),
    Defense("防御"),
    Disruption("中断"),
    Excavation("挖掘"),
    Sabotage("破坏"),
    Survival("生存"),
    Extermination("歼灭"),
    Free_Roam("自由漫步"),
    Hijack("劫持"),
    Hive("清巢"),
    Interception("拦截"),
    Mobile_Defense("移动防御"),
    Orphix("奥影母艇"),
    Rescue("救援"),
    Skirmish("前哨战"),
    Spy("间谍"),
    Volatile("爆发"),
    ;
    private final String CN;

    WarframeMissionTypeEnum(String cn) {
        this.CN = cn;
    }

    public String get() {
        return CN;
    }
}
