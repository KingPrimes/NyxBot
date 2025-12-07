package com.nyx.bot.utils;

import org.junit.jupiter.api.Test;

public class TestStringUtils {

    @Test
    void testGetWeaponsEnglishName() {
        String description = "这些凝聚虚空能量的锋利之刃能造成了深切而痛苦的伤口。";
        String substring = StringUtils.getSubString(description, "（英文：", "）");
        String name = StringUtils.convertToCamelCase(substring);
        System.out.println(name);
    }

    @Test
    void testSubstring(){
        String str = "-wsServerUrl=/ws/shiro";
        String substring = StringUtils.getSubString(str, "=", "");
        System.out.println(substring);
    }
}
