package com.nyx.bot.utils.http;

import io.github.kingprimes.model.enums.MarketPlatformEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;

/**
 * Http请求工具类</br>
 * 基于 Java 21 {@link HttpClient} 实现，使用异步非阻塞 I/O 避免虚拟线程 pin 平台线程
 *
 * @author KingPrimes
 */
@Slf4j
public class HttpUtils {

    /**
     * 请求超时时间（毫秒）
     * 延长至 10s 以适应国内→海外网络环境
     */
    private static final int CONNECT_TIMEOUT = 10000;
    /**
     * 读取超时（毫秒）
     * 延长至 30s 以适应慢速 API 响应
     */
    private static final int READ_TIMEOUT = 30000;

    /**
     * 默认的请求头
     */
    private static final Map<String, List<String>> DEFAULT_HEADERS;

    /**
     * Java 21 内置非阻塞 HttpClient，统一处理所有 HTTP 请求
     * 替代原有的 RestTemplate（client / insecureClient / noProxyClient）
     */
    private static final HttpClient HTTP_CLIENT;

    /*
      静态初始化
     */
    static {
        DEFAULT_HEADERS = new LinkedHashMap<>();
        DEFAULT_HEADERS.put(HttpHeaders.ACCEPT, List.of("*/*"));
        DEFAULT_HEADERS.put(HttpHeaders.CACHE_CONTROL, List.of("no-cache"));
        DEFAULT_HEADERS.put(HttpHeaders.PRAGMA, List.of("no-cache"));
        DEFAULT_HEADERS.put(HttpHeaders.USER_AGENT, List.of("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 Edg/142.0.0.0"));

        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
                .followRedirects(HttpClient.Redirect.NORMAL)
                // 信任所有 SSL 证书，解决国内访问海外 CDN/API 时证书链不完整的问题
                .sslContext(createInsecureSSLContext());

        // 复用原有的代理解析链（JVM参数 → 环境变量 → 系统代理 → Spring配置）
        Proxy proxy = ProxyUtils.getEffectiveProxyForUrl();
        if (!proxy.equals(Proxy.NO_PROXY) && proxy.address() instanceof InetSocketAddress addr) {
            builder.proxy(ProxySelector.of(addr));
        }

        HTTP_CLIENT = builder.build();
    }

    /**
     * 创建信任所有 SSL 证书的 SSLContext
     * 用于处理国内服务器访问海外 CDN/API 时证书链不完整或被 GFW 封锁导致的握手失败
     *
     * @return 信任所有证书的 SSLContext 实例
     */
    private static SSLContext createInsecureSSLContext() {
        try {
            // 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // 安装全信任的TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            log.error("创建忽略SSL的SSLContext失败", e);
            throw new RuntimeException("Failed to create insecure SSL context", e);
        }
    }

    // ══════════════════════════════════════════════
    // 包内可见 — 供 HttpFileDownloader 使用
    // ══════════════════════════════════════════════

    /**
     * 发送 POST 请求并返回字节数组响应（文件下载用）
     */
    static Body sendPostForBytes(String url, String json) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(READ_TIMEOUT))
                    .method("POST", HttpRequest.BodyPublishers.ofString(json));

            applyDefaultHeaders(builder, false);

            HttpResponse<byte[]> response = HTTP_CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body() != null) {
                return new Body("", response.statusCode(), response.headers().map(), url,
                        response.body());
            } else {
                log.warn("请求失败 code:{}, body:{}", response.statusCode(), response.body());
                return new Body(response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.warn("请求异常", e);
            return new Body(Body.CODE_UNKNOWN);
        }
    }

    /**
     * 发送 GET 请求并返回 InputStream 响应（流式文件下载用）
     */
    static HttpResponse<InputStream> sendGetForStream(String url) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(READ_TIMEOUT))
                .GET();

        applyDefaultHeaders(builder, false);

        return HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
    }

    /**
     * 设置默认请求头（复用逻辑）
     *
     * @param builder    请求构造器
     * @param isJsonBody true 表示 JSON 请求体，设置 application/json；否则设置 octet-stream
     */
    private static void applyDefaultHeaders(HttpRequest.Builder builder, boolean isJsonBody) {
        DEFAULT_HEADERS.forEach((k, v) -> v.forEach(val -> builder.header(k, val)));
        String contentType = isJsonBody ? MediaType.APPLICATION_JSON_VALUE : "application/octet-stream";
        builder.header("Content-Type", contentType);
        builder.header("Accept-Encoding", "application/octet-stream");
    }

    // ══════════════════════════════════════════════
    // 公共 API — GET 请求
    // ══════════════════════════════════════════════

    /**
     * Http Get请求
     */
    public static Body sendGet(String url) {
        return sendGet(url, "");
    }

    /**
     * Http Get请求
     */
    public static Body sendGet(String url, String param) {
        return sendGet(url, param, null);
    }

    /**
     * Http Get请求
     */
    public static Body sendGet(String url, Map<String, List<String>> headers) {
        return sendGet(url, "", headers);
    }

    /**
     * Http Get请求（无缓存）
     */
    public static Body sendGet(String url, String param, Map<String, List<String>> headers) {
        return sendGet(url, param, headers, 0);
    }

    /**
     * Http Get请求（支持缓存）
     */
    public static Body sendGet(String url, String param, Map<String, List<String>> headers, long cacheSeconds) {
        if (cacheSeconds > 0) {
            String key = HttpCacheManager.buildCacheKey(url, param);
            return getMarketBody(url, param, headers, cacheSeconds, key);
        }
        return doExchange(appendParam(url, param), "GET", null, headers);
    }

    /**
     * 带缓存的 GET 请求内部实现（先查缓存，未命中则发请求并写入缓存）
     */
    private static Body getMarketBody(String url, String param, Map<String, List<String>> headers, long cacheSeconds, String key) {
        Body cached = HttpCacheManager.cacheGet(key);
        if (cached != null) {
            return cached;
        }
        Body result = doExchange(appendParam(url, param), "GET", null, headers);
        if (result.is2xxSuccessful()) {
            HttpCacheManager.cachePut(key, result, cacheSeconds);
        }
        return result;
    }

    // ══════════════════════════════════════════════
    // 公共 API — Market 请求
    // ══════════════════════════════════════════════

    /**
     * Http Get请求 Market 交易
     */
    public static Body marketSendGet(String url) {
        return marketSendGet(url, "", MarketPlatformEnum.PC, 0);
    }

    /**
     * Http Get请求 Market 交易
     */
    public static Body marketSendGet(String url, String param) {
        return marketSendGet(url, param, MarketPlatformEnum.PC);
    }

    /**
     * Http Get请求 Market 交易（无缓存）
     */
    public static Body marketSendGet(String url, String param, MarketPlatformEnum form) {
        return marketSendGet(url, param, form, 120);
    }

    /**
     * Http Get请求 Market 交易（支持缓存）
     */
    public static Body marketSendGet(String url, String param, MarketPlatformEnum form, long cacheSeconds) {
        // 构建 Market API 专用请求头
        Map<String, List<String>> h = new LinkedHashMap<>();
        h.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
        h.put(HttpHeaders.ACCEPT_LANGUAGE, List.of("zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6"));
        h.put("Language", List.of("zh-hans"));
        h.put("Platform", List.of(form.getPlatform()));
        h.put("Pragma", List.of("no-cache"));
        h.put("Crossplay", List.of("true"));

        if (cacheSeconds > 0) {
            String key = HttpCacheManager.buildCacheKey(url, param + "_" + form.getPlatform());
            return getMarketBody(url, param, h, cacheSeconds, key);
        }
        return doExchange(appendParam(url, param), "GET", null, h);
    }

    // ══════════════════════════════════════════════
    // 公共 API — POST 请求
    // ══════════════════════════════════════════════

    /**
     * Http Post请求
     */
    public static Body sendPost(String url, String json) {
        Map<String, List<String>> h = new LinkedHashMap<>();
        h.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
        return doExchange(url, "POST", json, h);
    }

    // ══════════════════════════════════════════════
    // 内部实现
    // ══════════════════════════════════════════════

    /**
     * 执行HTTP请求交换操作
     * 使用 Java 21 HttpClient 的非阻塞 I/O，不会 pin 住虚拟线程
     */
    @SuppressWarnings("null")
    private static Body doExchange(String url, String method, Object requestBody,
                                   Map<String, List<String>> extraHeaders) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(READ_TIMEOUT));

            applyDefaultHeaders(builder, requestBody != null);
            if (extraHeaders != null) {
                extraHeaders.forEach((k, v) -> v.forEach(val -> builder.header(k, val)));
            }

            // 方法 + Body
            if (requestBody != null && "POST".equalsIgnoreCase(method)) {
                builder.method("POST", HttpRequest.BodyPublishers.ofString(requestBody.toString()));
            } else {
                builder.GET();
            }

            HttpResponse<String> response = HTTP_CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());

            return new Body(response.body(),
                    response.statusCode(),
                    response.headers().map(),
                    url, null);
        } catch (IOException | InterruptedException e) {
            log.warn("HTTP {} {} failed: {}", method, url, e.getMessage());
            return new Body(Body.CODE_UNKNOWN);
        }
    }

    /**
     * 拼接 URL 参数
     *
     * @param url   基础 URL
     * @param param 查询参数字符串
     * @return 拼接后的完整 URL
     */
    private static String appendParam(String url, String param) {
        if (param == null || param.isEmpty()) {
            return url;
        }
        return url + "?" + param;
    }

    // ══════════════════════════════════════════════
    // 网络工具
    // ══════════════════════════════════════════════

    /**
     * 获取本机 IPv4 地址
     *
     * @return 本机 IPv4 地址字符串；如果获取失败则返回 "0.0.0.0"
     */
    public static String getLocalIpv4() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            if (inetAddress instanceof Inet4Address) {
                return inetAddress.getHostAddress();
            }
            // 如果默认地址不是 IPv4，则遍历网络接口查找
            Enumeration<NetworkInterface> networkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException | UnknownHostException e) {
            log.error("getLocalIpv4 failed", e);
        }
        return "0.0.0.0";
    }

    /**
     * 获取本机 IPv6 地址
     *
     * @return 本机 IPv6 地址字符串；如果获取失败则返回 "::0"
     */
    public static String getLocalIpv6() {
        try {
            Enumeration<NetworkInterface> networkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet6Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("getLocalIpv6 failed", e);
        }
        return "::0";
    }

    // ══════════════════════════════════════════════
    // 响应体包装
    // ══════════════════════════════════════════════

    /**
     * HTTP 响应体包装记录
     * 使用 int 状态码和 Map 头信息，解耦 Spring 依赖
     */
    public record Body(String body, int code, Map<String, List<String>> headers, String url, byte[] file) {

        /**
         * 状态码未知时的默认值（请求异常/网络错误）
         */
        public static final int CODE_UNKNOWN = -1;

        /**
         * 仅状态码构造（用于错误响应）
         */
        public Body(int code) {
            this(null, code, Collections.emptyMap(), null, null);
        }

        /**
         * 仅文件字节数组构造（用于文件响应）
         */
        public Body(byte[] file) {
            this(null, CODE_UNKNOWN, Collections.emptyMap(), null, file);
        }

        /**
         * 响应体 + 状态码构造
         */
        public Body(String body, int code) {
            this(body, code, Collections.emptyMap(), null, null);
        }

        /**
         * 文件 + 状态码构造
         */
        public Body(byte[] file, int code) {
            this(null, code, Collections.emptyMap(), null, file);
        }

        /**
         * 响应体 + 状态码 + 头构造
         */
        public Body(String body, int code, Map<String, List<String>> headers) {
            this(body, code, headers, null, null);
        }

        /**
         * 响应体 + 状态码 + 头 + URL 构造
         */
        public Body(String body, int code, Map<String, List<String>> headers, String url) {
            this(body, code, headers, url, null);
        }

        /**
         * 判断响应状态码是否在 2xx 范围内（请求成功）
         *
         * @return true 表示 2xx 成功状态
         */
        public boolean is2xxSuccessful() {
            return code >= 200 && code < 300;
        }

        /**
         * 判断响应状态码是否在 3xx 范围内（重定向）
         *
         * @return true 表示 3xx 重定向状态
         */
        public boolean is3xxRedirection() {
            return code >= 300 && code < 400;
        }

        /**
         * 判断是否为 HTTP 429 Too Many Requests（速率限制）
         *
         * @return true 表示触发速率限制
         */
        public boolean isTooManyRequests() {
            return code == 429;
        }

        /**
         * 将响应头转换为单值 Map（每个 key 取第一个 value）
         * 用于日志输出等场景，替代 Spring HttpHeaders.toSingleValueMap()
         *
         * @return Map<String, String> 单值头信息
         */
        public Map<String, String> toSingleValueMap() {
            Map<String, String> result = new LinkedHashMap<>();
            if (headers != null) {
                headers.forEach((k, v) -> {
                    if (v != null && !v.isEmpty()) {
                        result.put(k, v.get(0));
                    }
                });
            }
            return result;
        }
    }

    // ══════════════════════════════════════════════
    // HTTP 头常量
    // ══════════════════════════════════════════════

    /**
     * HTTP 请求头常量（内部使用，替代 Spring HttpHeaders 的直接依赖）
     */
    static final class HttpHeaders {
        /**
         * 可接受的响应内容类型
         */
        static final String ACCEPT = "Accept";
        /**
         * 连接保持
         */
        static final String CONNECTION = "Connection";
        /**
         * 缓存控制
         */
        static final String CACHE_CONTROL = "Cache-Control";
        /**
         * 缓存验证
         */
        static final String PRAGMA = "Pragma";
        /**
         * 客户端标识
         */
        static final String USER_AGENT = "User-Agent";
        /**
         * 请求体内容类型
         */
        static final String CONTENT_TYPE = "Content-Type";
        /**
         * 可接受的语言
         */
        static final String ACCEPT_LANGUAGE = "Accept-Language";

        private HttpHeaders() {
        }
    }
}
