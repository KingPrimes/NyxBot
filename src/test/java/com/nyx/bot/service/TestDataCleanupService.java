package com.nyx.bot.service;

import com.nyx.bot.NyxBotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Transactional
public class TestDataCleanupService {

    @Autowired
    DataCleanupService dataCleanupService;

    @Test
    public void testPerformAtomicCleanup() {
        assertDoesNotThrow(() -> dataCleanupService.performAtomicCleanup());
    }

}
