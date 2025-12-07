package com.nyx.bot.utils;

import org.junit.jupiter.api.Test;

public class TestMatcherUtils {

    @Test
    void testGetNumber() {
        String str = "adgaew12345678awegw90";
        System.out.println(MatcherUtils.getNumber(str));
    }
}
