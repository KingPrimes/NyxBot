package com.nyx.bot.utils;


import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.utils.gitutils.GitHubUtil;
import com.nyx.bot.utils.gitutils.JgitUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestVersion {

    @Test
    void testGetJarVersion() {
        byte[] latestZip = GitHubUtil.getLatestZip();
        FileUtils.writeToFile(latestZip, "./tmp/NyxBot.jar");
    }

    @Test
    public void testGitUrl() {
        log.info(JgitUtil.getOriginUrl(JgitUtil.lockPath));
    }

}
