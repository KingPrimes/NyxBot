package com.nyx.bot.enums;

import lombok.Getter;

public enum RivenTrendTypeEnum {
    //步枪 - 狙击枪
    RIFLE((short) 0, "步枪-狙击枪"),
    //霰弹枪
    SHOTGUN((short) 1, "霰弹枪"),
    //手枪
    PISTOL((short) 2, "手枪"),
    //Archwing枪械
    ARCHGUN((short) 3, "Archwing枪械"),
    //近战
    MELEE((short) 4, "近战");

    final short value;

    @Getter
    final String desc;


    RivenTrendTypeEnum(short value, String desc) {
        this.desc = desc;
        this.value = value;
    }

    public Short value() {
        return value;
    }
}
