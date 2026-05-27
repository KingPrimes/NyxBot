package com.nyx.bot.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMatcherUtils {

    @Test
    void testGetNumber() {
        String str = "adgaew12345678awegw90";
        assertNotNull(MatcherUtils.getNumber(str));
    }
}
