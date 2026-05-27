package com.nyx.bot.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFileUtils {


    @Test
    void testGetFilesName() {
        assertNotNull(FileUtils.getFilesName("./logs"));
    }
}
