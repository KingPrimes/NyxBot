package com.nyx.bot.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nyx.bot.utils.gitutils.CdnTagResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CdnTagResolver 测试（使用 WireMock 模拟 GitHub Tags API，无需外部网络）
 */
class CdnTagResolverTest {

    private static final String TAGS_JSON = """
            [
              {"name": "v1.5.0", "zipball_url": "https://api.github.com/repos/KingPrimes/DataSource/zipball/v1.5.0"},
              {"name": "v1.4.0", "zipball_url": "https://api.github.com/repos/KingPrimes/DataSource/zipball/v1.4.0"}
            ]""";
    private static final String SINGLE_TAG_JSON = """
            [{"name": "v2.0.0"}]""";
    private static final String EMPTY_TAGS_JSON = "[]";
    private static WireMockServer wireMock;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        configureFor(wireMock.port());

        stubFor(get(urlPathEqualTo("/tags"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TAGS_JSON)));

        stubFor(get(urlPathEqualTo("/single-tag"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(SINGLE_TAG_JSON)));

        stubFor(get(urlPathEqualTo("/empty-tags"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(EMPTY_TAGS_JSON)));

        stubFor(get(urlPathEqualTo("/server-error"))
                .willReturn(aResponse().withStatus(500)));

        stubFor(get(urlPathEqualTo("/garbage"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                        .withBody("not json")));
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    /**
     * 通过反射替换 CdnTagResolver 中的 GITHUB_TAGS_URL 为 WireMock 地址，
     * 使测试直接覆盖生产代码路径。
     */
    private static void setGithubTagsUrl(String url) throws Exception {
        Field field = CdnTagResolver.class.getDeclaredField("GITHUB_TAGS_URL");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, url);
    }

    private String mockUrl(String path) {
        return "http://localhost:" + wireMock.port() + path;
    }

    // ══════════════════════════════════════════════
    // getLatestTag 测试 — 直接调用生产代码
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("getLatestTag — 成功获取标签并返回最新版本（第一个）")
    void testGetLatestTagSuccess() throws Exception {
        setGithubTagsUrl(mockUrl("/tags"));
        assertEquals("v1.5.0", CdnTagResolver.getLatestTag());
    }

    @Test
    @DisplayName("getLatestTag — 单标签数组返回唯一标签")
    void testGetLatestTagSingleTag() throws Exception {
        setGithubTagsUrl(mockUrl("/single-tag"));
        assertEquals("v2.0.0", CdnTagResolver.getLatestTag());
    }

    @Test
    @DisplayName("getLatestTag — 空标签数组回退到 latest")
    void testGetLatestTagEmptyArray() throws Exception {
        setGithubTagsUrl(mockUrl("/empty-tags"));
        assertEquals("latest", CdnTagResolver.getLatestTag());
    }

    @Test
    @DisplayName("getLatestTag — 服务器 500 错误回退到 latest")
    void testGetLatestTagServerError() throws Exception {
        setGithubTagsUrl(mockUrl("/server-error"));
        assertEquals("latest", CdnTagResolver.getLatestTag());
    }

    @Test
    @DisplayName("getLatestTag — 非 JSON 响应回退到 latest")
    void testGetLatestTagGarbageResponse() throws Exception {
        setGithubTagsUrl(mockUrl("/garbage"));
        assertEquals("latest", CdnTagResolver.getLatestTag());
    }

    // ══════════════════════════════════════════════
    // buildUrls 测试 — 直接调用生产代码
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("buildUrls — jsDelivr URL 拼接版本号")
    void testBuildUrlsWithTag() throws Exception {
        setGithubTagsUrl(mockUrl("/tags"));
        List<String> urls = CdnTagResolver.buildUrls("warframe/alias.json");
        assertEquals(4, urls.size());
        assertTrue(urls.get(0).contains("@v1.5.0/"));
        assertTrue(urls.get(1).contains("@v1.5.0/"));
        assertTrue(urls.get(2).contains("@v1.5.0/"));
        assertTrue(urls.get(3).startsWith("https://kingprimes.top/"));
        assertFalse(urls.get(3).contains("@"));
    }

    @Test
    @DisplayName("buildUrls — kingprimes.top 直接拼接路径，不含版本号")
    void testBuildUrlsKingPrimesDirect() throws Exception {
        setGithubTagsUrl(mockUrl("/tags"));
        List<String> urls = CdnTagResolver.buildUrls("warframe/nodes.json");
        assertEquals("https://kingprimes.top/warframe/nodes.json", urls.get(3));
    }

    @Test
    @DisplayName("buildUrls — 回退到 latest 时也正确拼接")
    void testBuildUrlsFallbackLatest() throws Exception {
        setGithubTagsUrl(mockUrl("/server-error"));
        List<String> urls = CdnTagResolver.buildUrls("warframe/reward_pool.json");
        assertEquals(4, urls.size());
        assertTrue(urls.get(0).contains("@latest/"));
    }

    @Test
    @DisplayName("buildUrls — 空标签拼接")
    void testBuildUrlsEmptyTag() throws Exception {
        setGithubTagsUrl(mockUrl("/empty-tags"));
        List<String> urls = CdnTagResolver.buildUrls("warframe/alias.json");
        assertEquals(4, urls.size());
        assertTrue(urls.get(0).endsWith("DataSource@/warframe/alias.json"));
    }
}
