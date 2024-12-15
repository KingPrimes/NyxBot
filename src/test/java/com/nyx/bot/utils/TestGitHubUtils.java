package com.nyx.bot.utils;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.utils.gitutils.GitHubUtil;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER,
        properties = {
                "spring.sendgrid.proxy.host=127.0.0.1",
                "spring.sendgrid.proxy.port=7890"
        })
public class TestGitHubUtils {

    //测试下载最新的Jar文件
    @Test
    void testGithubGetLatestZip() {
        log.info("testGithubGetLatestZip:{}", HttpUtils.sendGetForFile(GitHubUtil.getLatestDownLoadUrl(), "./tmp/NyxBot.jar"));
    }

    //测试获取最新版本的下载链接
    @Test
    void testGitHubGetLatestDownLoadUrl() {
        log.info("testGitHubGetLatestDownLoadUrl:{}", GitHubUtil.getLatestDownLoadUrl());
    }

    // 测试获取最新版本
    @Test
    void testGitHubGetReleasesLatestVersion() {
        log.info("testGitHubGetReleasesLatestVersion:{}", GitHubUtil.getReleasesLatestVersion("KingPrimes", "NyxBot"));
    }

}
