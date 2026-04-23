package com.nyx.bot.utils;

import com.nyx.bot.modules.warframe.utils.RivenMatcherUtil;
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

    @Test
    void testRivenAttributes(){
        // [[委, 181, 豪猪 Acri-satimag, +108.8% 暴击伤害, -93.1% 武器后坐力, +132.9% 多重射击, x0.63 对 Corpus 的伤害, 段位8]]
        String str = "x0.63对Corpus的伤害";
        System.out.println(RivenMatcherUtil.isAttribute(str));
        String attributeName = RivenMatcherUtil.getAttributeName(str);
        System.out.println(attributeName);
        System.out.println("暴击几率重击".matches("暴击几率\\(?（?重?击?时?"));
    }
}
