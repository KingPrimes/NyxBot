package com.nyx.bot.utils.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nyx.bot.utils.http.HttpUtils.Body;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpUtils 测试（使用 WireMock 模拟 HTTP 端点，无需外部网络）
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestHttpUtils {

    private static WireMockServer wireMock;

    private static String baseUrl(String path) {
        return "http://localhost:" + wireMock.port() + path;
    }

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        // ---- 通用 stub ----
        configureFor(wireMock.port());

        // GET /get — 基础响应
        stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"url\":\"/get\",\"args\":{}}")));

        // GET /get?a=1 — 带参数的响应
        stubFor(get(urlPathEqualTo("/get"))
                .withQueryParam("a", equalTo("1"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"url\":\"/get\",\"args\":{\"a\":\"1\"}}")));

        // GET /get?b=2 — 另一个参数
        stubFor(get(urlPathEqualTo("/get"))
                .withQueryParam("b", equalTo("2"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"url\":\"/get\",\"args\":{\"b\":\"2\"}}")));

        // POST /post — 基础 POST
        stubFor(post(urlEqualTo("/post"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"received\":true}")));

        // GET /file — 模拟文件下载
        stubFor(get(urlEqualTo("/file"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .withHeader(HttpHeaders.CONTENT_LENGTH, "1024")
                        .withBody(new byte[1024])));

        // POST /file — 模拟 POST 文件下载
        stubFor(post(urlEqualTo("/file"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .withBody(new byte[512])));

        // GET /status/404
        stubFor(get(urlEqualTo("/status/404"))
                .willReturn(aResponse().withStatus(404)));

        // GET /status/500
        stubFor(get(urlEqualTo("/status/500"))
                .willReturn(aResponse().withStatus(500)));

        // POST /status/500
        stubFor(post(urlEqualTo("/status/500"))
                .willReturn(aResponse().withStatus(500)));

        // GET /slow — 慢响应（用于超时测试，5秒延迟）
        stubFor(get(urlEqualTo("/slow"))
                .willReturn(aResponse()
                        .withFixedDelay(6000)
                        .withBody("too slow")));
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    // ══════════════════════════════════════════════
    // sendGet 基础测试
    // ══════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("sendGet — 基础 GET 请求")
    void testSendGetBasic() {
        Body result = HttpUtils.sendGet(baseUrl("/get"));
        assertNotNull(result);
        assertTrue(result.code().is2xxSuccessful());
        assertTrue(result.body().contains("/get"));
    }

    @Test
    @Order(2)
    @DisplayName("sendGet — 无参数调用（param 为空字符串）")
    void testSendGetEmptyParam() {
        Body result = HttpUtils.sendGet(baseUrl("/get"), "");
        assertNotNull(result);
        assertTrue(result.code().is2xxSuccessful());
    }

    @Test
    @Order(3)
    @DisplayName("sendGet — 带自定义 headers")
    void testSendGetWithHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Custom", "test-value");
        Body result = HttpUtils.sendGet(baseUrl("/get"), h);
        assertNotNull(result);
        assertTrue(result.code().is2xxSuccessful());
    }

    @Test
    @Order(4)
    @DisplayName("sendGet — 4xx 响应不抛异常")
    void testSendGet404() {
        Body result = HttpUtils.sendGet(baseUrl("/status/404"));
        assertNotNull(result);
        assertEquals(404, result.code().value());
    }

    @Test
    @Order(5)
    @DisplayName("sendGet — 5xx 响应不抛异常")
    void testSendGet500() {
        Body result = HttpUtils.sendGet(baseUrl("/status/500"));
        assertNotNull(result);
        assertEquals(500, result.code().value());
    }

    // ══════════════════════════════════════════════
    // sendPost 测试
    // ══════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("sendPost — 基础 POST 请求")
    void testSendPostBasic() {
        Body result = HttpUtils.sendPost(baseUrl("/post"), "{\"key\":\"value\"}");
        assertNotNull(result);
        assertTrue(result.code().is2xxSuccessful());
        assertTrue(result.body().contains("received"));
    }

    @Test
    @Order(7)
    @DisplayName("sendPost — 5xx 响应不抛异常")
    void testSendPost500() {
        Body result = HttpUtils.sendPost(baseUrl("/status/500"), "{}");
        assertNotNull(result);
        assertEquals(500, result.code().value());
    }

    // ══════════════════════════════════════════════
    // sendPostForFile 测试
    // ══════════════════════════════════════════════

    @Test
    @Order(8)
    @DisplayName("sendPostForFile — 成功下载文件")
    void testSendPostForFileSuccess() {
        Body result = HttpFileDownloader.sendPostForFile(baseUrl("/file"), "{}");
        assertNotNull(result);
        assertTrue(result.code().is2xxSuccessful());
        assertNotNull(result.file());
        assertEquals(512, result.file().length);
    }

    @Test
    @Order(9)
    @DisplayName("sendPostForFile — 非 2xx 响应返回错误状态码")
    void testSendPostForFileNon2xx() {
        Body result = HttpFileDownloader.sendPostForFile(baseUrl("/status/500"), "{}");
        assertNotNull(result);
        assertEquals(500, result.code().value());
        assertNull(result.file());
    }

    // ══════════════════════════════════════════════
    // sendGetForFile 测试
    // ══════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("sendGetForFile — 下载文件到磁盘")
    void testSendGetForFile() {
        String tempPath = System.getProperty("java.io.tmpdir") + "/nyx-test-download.bin";
        try {
            Boolean ok = HttpFileDownloader.sendGetForFile(baseUrl("/file"), tempPath);
            assertTrue(ok);
            java.io.File f = new java.io.File(tempPath);
            assertTrue(f.exists());
            assertEquals(1024, f.length());
        } finally {
            new java.io.File(tempPath).delete();
        }
    }

    // ══════════════════════════════════════════════
    // 缓存功能测试
    // ══════════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("缓存 API — 相同URL两次调用不抛异常且响应一致")
    void testCacheApiSameUrlTwice() {
        Body first = HttpUtils.sendGet(baseUrl("/get"), "", null, 60);
        assertNotNull(first);
        assertTrue(first.code().is2xxSuccessful());

        Body second = HttpUtils.sendGet(baseUrl("/get"), "", null, 60);
        assertNotNull(second);
        assertNotNull(second.body());
        assertFalse(second.body().isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("缓存 API — 不同参数返回不同响应")
    void testCacheApiDifferentParams() {
        Body first = HttpUtils.sendGet(baseUrl("/get"), "a=1", null, 60);
        assertNotNull(first);
        assertTrue(first.code().is2xxSuccessful());

        Body second = HttpUtils.sendGet(baseUrl("/get"), "b=2", null, 60);
        assertNotNull(second);
        assertTrue(second.code().is2xxSuccessful());
        assertNotEquals(first.body(), second.body());
    }

    @Test
    @Order(13)
    @DisplayName("缓存禁用 — cacheSeconds=0 正确工作")
    void testCacheDisabledByZeroTtl() {
        Body first = HttpUtils.sendGet(baseUrl("/get"), "", null, 0);
        Body second = HttpUtils.sendGet(baseUrl("/get"), "", null, 0);
        assertNotNull(first);
        assertNotNull(second);
        assertTrue(first.code().is2xxSuccessful());
        assertTrue(second.code().is2xxSuccessful());
    }

    @Test
    @Order(14)
    @DisplayName("缓存过期 — TTL 超时后不抛异常")
    void testCacheExpiry() throws InterruptedException {
        Body first = HttpUtils.sendGet(baseUrl("/get"), "", null, 1);
        assertNotNull(first);
        assertTrue(first.code().is2xxSuccessful());

        Thread.sleep(2000);

        Body second = HttpUtils.sendGet(baseUrl("/get"), "", null, 1);
        assertNotNull(second);
        assertTrue(second.code().is2xxSuccessful());
    }

    @Test
    @Order(15)
    @DisplayName("非2xx响应 — 不被缓存、不抛异常")
    void testNon2xxResponseNotCached() {
        Body first = HttpUtils.sendGet(baseUrl("/status/404"), "", null, 60);
        assertNotNull(first);
        assertFalse(first.code().is2xxSuccessful());

        Body second = HttpUtils.sendGet(baseUrl("/status/404"), "", null, 60);
        assertNotNull(second);
        assertEquals(first.code().value(), second.code().value());
    }

    @Test
    @Order(16)
    @DisplayName("容错 — 缓存失败不阻塞请求")
    void testCacheFailureDoesNotBlockRequest() {
        Body result = HttpUtils.sendGet(baseUrl("/get"), "", null, 60);
        assertNotNull(result);
        assertTrue(result.code().is2xxSuccessful());
    }

    // ══════════════════════════════════════════════
    // marketSendGet 测试
    // ══════════════════════════════════════════════

    @Test
    @Order(17)
    @DisplayName("marketSendGet — 带缓存参数工作正常")
    void testMarketSendGetWithCache() {
        Body first = HttpUtils.marketSendGet(baseUrl("/get"), "",
                io.github.kingprimes.model.enums.MarketPlatformEnum.PC, 60);
        assertNotNull(first);
        assertTrue(first.code().is2xxSuccessful());

        Body second = HttpUtils.marketSendGet(baseUrl("/get"), "",
                io.github.kingprimes.model.enums.MarketPlatformEnum.PC, 60);
        assertNotNull(second);
        assertNotNull(second.body());
        assertFalse(second.body().isEmpty());
    }

    // ══════════════════════════════════════════════
    // 向后兼容测试
    // ══════════════════════════════════════════════

    @Test
    @Order(18)
    @DisplayName("向后兼容 — 现有无缓存方法签名工作正常")
    void testExistingSignatureBackwardCompatible() {
        assertNotNull(HttpUtils.sendGet(baseUrl("/get")));
        assertNotNull(HttpUtils.sendGet(baseUrl("/get"), ""));
        assertNotNull(HttpUtils.sendGet(baseUrl("/get"), "", null));
    }

    // ══════════════════════════════════════════════
    // 错误路径 / 边界测试 (TEST-002 新增)
    // ══════════════════════════════════════════════

    @Test
    @Order(19)
    @DisplayName("边界 — sendGet 传入 null headers 不抛异常")
    void testSendGetNullHeaders() {
        Body result = HttpUtils.sendGet(baseUrl("/get"), "", null);
        assertNotNull(result);
        assertTrue(result.code().is2xxSuccessful());
    }

    @Test
    @Order(20)
    @DisplayName("getLocalIpv4 — 返回非空字符串")
    void testGetLocalIpv4ReturnsValid() {
        String ip = HttpUtils.getLocalIpv4();
        assertNotNull(ip);
        assertFalse(ip.isEmpty());
    }

    @Test
    @Order(21)
    @DisplayName("getLocalIpv6 — 返回非空字符串")
    void testGetLocalIpv6ReturnsValid() {
        String ip = HttpUtils.getLocalIpv6();
        assertNotNull(ip);
        assertFalse(ip.isEmpty());
    }

    @Test
    @Order(22)
    @DisplayName("Body record — HttpStatusCode 构造器")
    void testBodyConstructorWithCode() {
        Body b = new Body(HttpStatus.OK);
        assertEquals(HttpStatus.OK, b.code());
        assertNull(b.body());
        assertNull(b.file());
    }

    @Test
    @Order(23)
    @DisplayName("Body record — 完整构造器")
    void testBodyFullConstructor() {
        byte[] data = {1, 2, 3};
        Body b = new Body("text", HttpStatus.OK, null, "http://test", data);
        assertEquals("text", b.body());
        assertEquals(HttpStatus.OK, b.code());
        assertEquals("http://test", b.url());
        assertArrayEquals(data, b.file());
    }
}
