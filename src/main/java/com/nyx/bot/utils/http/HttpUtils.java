package com.nyx.bot.utils.http;

import io.github.kingprimes.model.enums.MarketPlatformEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Http请求工具类</br>
 * 对{@link RestTemplate}进行封装，提供更方便的使用方式
 *
 * @author KingPrimes
 */
@Slf4j
@SuppressWarnings("null")
public class HttpUtils {

    /**
     * 默认的请求头
     */
    private static final HttpHeaders headers;
    /**
     * 默认的 RestTemplate
     */
    private static final RestTemplate client;
    /**
     * 忽略SSL验证的 RestTemplate
     */
    private static final RestTemplate insecureClient;
    /**
     * 无代理的 RestTemplate（用于 *.warframe.com）
     */
    private static final RestTemplate noProxyClient;
    /**
     * 请求超时时间
     */
    private static final int CONNECT_TIMEOUT = 5000;
    /**
     * 读取超时
     */
    private static final int READ_TIMEOUT = 15000;

    /*
      静态初始化
     */
    static {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        //调用超时
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        //读取超时
        requestFactory.setReadTimeout(READ_TIMEOUT);

        requestFactory.setProxy(ProxyUtils.getEffectiveProxyForUrl());

        client = new RestTemplate(requestFactory);
        insecureClient = createInsecureRestTemplate();

        // 无代理的 RestTemplate（用于 *.warframe.com）
        SimpleClientHttpRequestFactory noProxyFactory = new SimpleClientHttpRequestFactory();
        noProxyFactory.setConnectTimeout(CONNECT_TIMEOUT);
        noProxyFactory.setReadTimeout(READ_TIMEOUT);
        noProxyFactory.setProxy(Proxy.NO_PROXY);
        noProxyClient = new RestTemplate(noProxyFactory);
        headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "*/*");
        headers.add(HttpHeaders.CONNECTION, "keep-alive");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 Edg/142.0.0.0");
    }

    /**
     * 创建忽略SSL证书验证的RestTemplate
     * 用于处理cdn.jsdelivr.net等证书链不完整的网站
     *
     * @return 忽略SSL验证的RestTemplate实例
     */
    private static RestTemplate createInsecureRestTemplate() {
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

            // 创建忽略SSL验证的ConnectionFactory
            SimpleClientHttpRequestFactory factory = getSimpleClientHttpRequestFactory(sslContext);

            return new RestTemplate(factory);
        } catch (Exception e) {
            log.error("创建忽略SSL的RestTemplate失败，将使用默认RestTemplate", e);
            return client;
        }
    }

    /**
     * 创建带有SSL配置的 RequestFactory（跳过证书验证，仅信任已知CDN）
     */
    private static SimpleClientHttpRequestFactory getSimpleClientHttpRequestFactory(SSLContext sslContext) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                if (connection instanceof HttpsURLConnection httpsConn) {
                    httpsConn.setSSLSocketFactory(sslContext.getSocketFactory());
                    // 仅信任已知CDN域名，防止MITM攻击泛化到任意目标
                    httpsConn.setHostnameVerifier((hostname, session) ->
                            hostname != null && (hostname.endsWith("jsdelivr.net") || hostname.endsWith("kingprimes.top")));
                }
            }
        };

        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        factory.setProxy(ProxyUtils.getEffectiveProxyForUrl());
        return factory;
    }

    // ══════════════════════════════════════════════
    // 包内可见 — 供 HttpFileDownloader / HttpCacheManager 使用
    // ══════════════════════════════════════════════

    /**
     * 获取默认请求头（供同包工具类使用）
     */
    static HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * 根据URL选择对应的 RestTemplate（供同包工具类使用）
     */
    static RestTemplate getRestTemplateForUrl(String url) {
        if (url.contains("warframe.com")) {
            return noProxyClient;
        }
        if (url.contains("cdn.jsdelivr.net") || url.contains("kingprimes.top")) {
            return insecureClient;
        }
        return client;
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
    public static Body sendGet(String url, HttpHeaders headers) {
        return sendGet(url, "", headers);
    }

    /**
     * Http Get请求（无缓存）
     */
    public static Body sendGet(String url, String param, HttpHeaders headers) {
        return sendGet(url, param, headers, 0);
    }

    /**
     * Http Get请求（支持缓存）
     */
    public static Body sendGet(String url, String param, HttpHeaders headers, long cacheSeconds) {
        if (cacheSeconds > 0) {
            String key = HttpCacheManager.buildCacheKey(url, param);
            return getMarketBody(url, param, headers, cacheSeconds, key);
        }
        return doExchange(appendParam(url, param), HttpMethod.GET, null, headers);
    }

    private static Body getMarketBody(String url, String param, HttpHeaders headers, long cacheSeconds, String key) {
        Body cached = HttpCacheManager.cacheGet(key);
        if (cached != null) {
            return cached;
        }
        Body result = doExchange(appendParam(url, param), HttpMethod.GET, null, headers);
        if (result.code() != null && result.code().is2xxSuccessful()) {
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
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        h.add("Language", "zh-hans");
        h.add("Platform", form.getPlatform());
        h.add("Pragma", "no-cache");
        h.add("Crossplay", "true");

        if (cacheSeconds > 0) {
            String key = HttpCacheManager.buildCacheKey(url, param + "_" + form.getPlatform());
            return getMarketBody(url, param, h, cacheSeconds, key);
        }
        return doExchange(appendParam(url, param), HttpMethod.GET, null, h);
    }

    // ══════════════════════════════════════════════
    // 公共 API — POST 请求
    // ══════════════════════════════════════════════

    /**
     * Http Post请求
     */
    public static Body sendPost(String url, String json) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return doExchange(url, HttpMethod.POST, json, h);
    }

    // ══════════════════════════════════════════════
    // 内部实现
    // ══════════════════════════════════════════════

    /**
     * 执行HTTP请求交换操作
     */
    private static Body doExchange(String url, HttpMethod method, Object requestBody,
                                   HttpHeaders extraHeaders) {
        HttpHeaders hers = new HttpHeaders(headers);
        if (extraHeaders != null) hers.putAll(extraHeaders);

        HttpEntity<?> req = (requestBody != null)
                ? new HttpEntity<>(requestBody, hers)
                : new HttpEntity<>(hers);

        try {
            ResponseEntity<String> resp =
                    getRestTemplateForUrl(url)
                            .exchange(url, method, req, String.class);

            String textBody = resp.getBody() != null ? resp.getBody() : "";
            return new Body(textBody, resp.getStatusCode(), resp.getHeaders(), url, null);
        } catch (RestClientException e) {
            log.warn("HTTP {} {} {}", method, url, e.getMessage());
            // 保留原始HTTP状态码，避免4xx/5xx被统一转为500
            HttpStatusCode statusCode = (e instanceof HttpStatusCodeException hsce)
                    ? hsce.getStatusCode()
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return new Body(statusCode);
        }
    }

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

    public record Body(String body, HttpStatusCode code, HttpHeaders headers, String url, byte[] file) {
        public Body(HttpStatusCode code) {
            this(null, code, null, null, null);
        }

        public Body(byte[] file) {
            this(null, null, null, null, file);
        }

        public Body(String body, HttpStatusCode code) {
            this(body, code, null, null, null);
        }

        public Body(byte[] file, HttpStatusCode code) {
            this(null, code, null, null, file);
        }

        public Body(String body, HttpStatusCode code, HttpHeaders headers) {
            this(body, code, headers, null, null);
        }

        public Body(String body, HttpStatusCode code, HttpHeaders headers, String url) {
            this(body, code, headers, url, null);
        }
    }
}
