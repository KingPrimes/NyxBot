package com.nyx.bot.utils;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.utils.gitutils.GitHubUtil;
import com.nyx.bot.utils.gitutils.JgitUtil;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
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

    @Test
    void testGitClone() throws GitAPIException {
        JgitUtil.Build("https://github.com/KingPrimes/DataSource","./data111").gitClone();
    }

}
