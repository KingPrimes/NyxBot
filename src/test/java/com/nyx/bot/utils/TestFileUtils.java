package com.nyx.bot.utils;

import org.junit.jupiter.api.Test;

public class TestFileUtils {


    @Test
    void testGetFilesName() {
        FileUtils.getFilesName("./logs").ifPresent(System.out::println);
    }
}
