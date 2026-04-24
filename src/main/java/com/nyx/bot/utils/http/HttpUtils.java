package com.nyx.bot.utils.http;

import com.nyx.bot.utils.FileUtils;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.security.cert.X509Certificate;

/**
 * Http请求工具类</br>
 * 对{@link RestTemplate}进行封装，提供更方便的使用方式
 *
 * @author KingPrimes
 */
@Slf4j
@SuppressWarnings({ "ALL", "null" })
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
     * 请求超时时间
     */
    private static final int CONNECT_TIMEOUT = 5000;
    /**
     * 读取超时
     */
    private static final int READ_TIMEOUT = 15000;
    /**
     * 上传进度
     */
    private static double lastProgress = -1;

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
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };

            // 安装全信任的TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 创建忽略SSL验证的ConnectionFactory
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws IOException {
                    super.prepareConnection(connection, httpMethod);
                    if (connection instanceof HttpsURLConnection) {
                        ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                        ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
                    }
                }
            };

            factory.setConnectTimeout(CONNECT_TIMEOUT);
            factory.setReadTimeout(READ_TIMEOUT);
            factory.setProxy(ProxyUtils.getEffectiveProxyForUrl());

            return new RestTemplate(factory);
        } catch (Exception e) {
            log.error("创建忽略SSL的RestTemplate失败，将使用默认RestTemplate", e);
            return client;
        }
    }

    /**
     * Http Get请求
     *
     * @param url 请求地址
     * @return 响应结果
     */
    public static Body sendGet(String url) {
        return sendGet(url, "");
    }

    /**
     * Http Get请求
     *
     * @param url   请求地址
     * @param param 请求参数
     * @return 响应结果
     */
    public static Body sendGet(String url, String param) {
        return sendGet(url, param, null);
    }

    /**
     * Http Get请求
     *
     * @param url     请求地址
     * @param headers 请求头
     * @return 响应结果
     */
    public static Body sendGet(String url, HttpHeaders headers) {
        return sendGet(url, "", headers);
    }

    /**
     * Http Get请求
     *
     * @param url     请求地址
     * @param param   请求参数
     * @param headers 请求头
     * @return 返回的文本
     */
    public static Body sendGet(String url, String param, HttpHeaders headers) {
        return doExchange(
                appendParam(url, param),
                HttpMethod.GET,
                null,
                headers,
                String.class
        );
    }

    /**
     * Http Get请求 Market 交易
     *
     * @param url 链接
     * @return 响应结果
     */
    public static Body marketSendGet(String url) {
        return marketSendGet(url, "", MarketPlatformEnum.PC);
    }

    /**
     * Http Get请求 Market 交易
     *
     * @param url   链接
     * @param param 参数
     * @return 响应结果
     */
    public static Body marketSendGet(String url, String param) {
        return marketSendGet(url, param, MarketPlatformEnum.PC);
    }

    /**
     * Http Get请求 Market 交易
     *
     * @param url   链接
     * @param param 参数
     * @param form  平台
     * @return 响应结果
     */
    public static Body marketSendGet(String url, String param, MarketPlatformEnum form) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        h.add("Language", "zh-hans");
        h.add("Platform", form.getPlatform());
        h.add("Pragma", "no-cache");
        h.add("Crossplay", "true");
        return doExchange(appendParam(url, param), HttpMethod.GET, null, h, String.class);
    }


    /**
     * Http Post请求
     *
     * @param url  请求地址
     * @param json 请求参数
     * @return 响应结果
     */
    public static Body sendPost(String url, String json) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return doExchange(url, HttpMethod.POST, json, h, String.class);
    }

    /**
     * 根据URL网址获取文件
     *
     * @param url  - url
     * @param path - 文件输出路径
     */
    public static Boolean sendGetForFile(String url, String path) {
        File outputFile = new File(path);
        // 若目录不存在,创建目录
        FileUtils.createDir(outputFile);
        return downloadFile(url, HttpMethod.GET, null, null, outputFile);
    }

    /**
     * 发送Post请求 获取文件
     *
     * @param url  - url
     * @param json Json格式的请求参数
     * @return byte[] 文件
     */
    public static Body sendPostForFile(String url, String json) {
        try {
            HttpHeaders h = new HttpHeaders(headers);
            h.setContentType(MediaType.APPLICATION_JSON);
            h.add("Accept-Encoding", "application/octet-stream");
            HttpEntity<String> request = new HttpEntity<>(json, h);
            ResponseEntity<Resource> response = getRestTemplateWithoutProxyIfNeeded(url).exchange(url, HttpMethod.POST, request, Resource.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("请求失败 code:{}, message:{}", response.getStatusCode().value(), response.getBody());
                return new Body(response.getStatusCode());
            }
            return new Body("", response.getStatusCode(), response.getHeaders(), url, response.getBody().getContentAsByteArray());
        } catch (RestClientException e) {
            log.warn("请求异常", e);
            return new Body(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.warn("请求异常", e);
            return new Body(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据URL判断是否需要使用无代理的RestTemplate
     * <p>*.warframe.com 不使用代理</p>
     *
     * @param url URL地址
     * @return RestTemplate实例
     */
    private static RestTemplate getRestTemplateWithoutProxyIfNeeded(String url) {
        if (url.matches(".*\\.?warframe\\.com.*")) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(CONNECT_TIMEOUT);
            factory.setReadTimeout(READ_TIMEOUT);
            factory.setProxy(Proxy.NO_PROXY);
            return new RestTemplate(factory);
        }
        // 对于cdn.jsdelivr.net等存在SSL证书问题的域名，使用忽略SSL验证的客户端
        if (url.contains("cdn.jsdelivr.net")) {
            return insecureClient;
        }
        return client;
    }

    /**
     * 打印文件下载进度
     *
     * @param fileSize   文件总大小
     * @param downloaded 已下载的字节数
     */
    private static void printDownloadProgress(long fileSize, long downloaded) {
        double progress = (double) downloaded / fileSize * 100;
        progress = Math.floor(progress); // Round down to nearest integer

        if (progress - lastProgress >= 1) {
            log.debug("文件下载进度:{}%", String.format("%.2f", progress));
            lastProgress = progress;
        }
    }

    /**
     * 执行HTTP请求交换操作
     *
     * @param url          请求URL
     * @param method       HTTP方法
     * @param requestBody  请求体内容
     * @param extraHeaders 额外的请求头
     * @param responseType 响应类型
     * @param <T>          响应泛型类型
     * @return Body 响应结果包装对象
     */
    private static <T> Body doExchange(String url, HttpMethod method, Object requestBody, HttpHeaders extraHeaders, Class<T> responseType) {
        HttpHeaders hers = new HttpHeaders(headers);
        if (extraHeaders != null) hers.putAll(extraHeaders);

        HttpEntity<?> req = (requestBody != null)
                ? new HttpEntity<>(requestBody, hers)
                : new HttpEntity<>(hers);

        try {
            ResponseEntity<T> resp =
                    getRestTemplateWithoutProxyIfNeeded(url)
                            .exchange(url, method, req, responseType);

            return new Body(
                    resp.getBody() instanceof byte[]
                            ? ""
                            : (String) resp.getBody(),
                    resp.getStatusCode(),
                    resp.getHeaders(),
                    url,
                    resp.getBody() instanceof byte[]
                            ? (byte[]) resp.getBody()
                            : null
            );
        } catch (RestClientException e) {
            log.error("HTTP {} {} failed", method, url, e);
            return new Body(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 将参数追加到URL中
     *
     * @param url   原始URL
     * @param param 要追加的参数
     * @return 拼接后的URL
     */
    private static String appendParam(String url, String param) {
        String urlNameString = url;
        if (!param.isEmpty()) {
            urlNameString = url + "?" + param;
        }
        return urlNameString;
    }

    /**
     * 下载文件
     *
     * @param url          请求URL
     * @param method       HTTP方法
     * @param extraHeaders 额外的请求头
     * @param output       输出文件
     * @return boolean 下载是否成功
     */
    private static boolean downloadFile(String url, HttpMethod method, Object requestBody, HttpHeaders extraHeaders, File output) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept-Encoding", "application/octet-stream");
        HttpHeaders hers = new HttpHeaders(headers);
        if (extraHeaders != null) hers.putAll(extraHeaders);

        HttpEntity<?> req = (requestBody != null)
                ? new HttpEntity<>(requestBody, hers)
                : new HttpEntity<>(hers);

        ResponseEntity<Resource> response = getRestTemplateWithoutProxyIfNeeded(url)
                .exchange(url, method,
                        req,
                        Resource.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try (InputStream in = response.getBody().getInputStream();
                 FileOutputStream out = new FileOutputStream(output)) {

                byte[] buf = new byte[1024];
                int n;
                long read = 0, total = output.length();
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    read += n;
                    printDownloadProgress(total, read);
                }
                return true;
            } catch (Exception e) {
                log.error("downloadFile failed for {} Handers:{}", url, response.getHeaders(), e);
                return false;
            }
        }
        log.warn("downloadFile failed for {} Handers:{}", url, response.getHeaders());
        return false;

    }

    /**
     * 获取本机 IPv4 地址
     *
     * @return 本机 IPv4 地址字符串；如果获取失败则返回 "0.0.0.0"
     */
    public static String getLocalIpv4() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            if (inetAddress instanceof java.net.Inet4Address) {
                return inetAddress.getHostAddress();
            }
            // 如果默认地址不是 IPv4，则遍历网络接口查找
            java.util.Enumeration<java.net.NetworkInterface> networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = networkInterfaces.nextElement();
                java.util.Enumeration<java.net.InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof java.net.Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
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
            java.util.Enumeration<java.net.NetworkInterface> networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = networkInterfaces.nextElement();
                java.util.Enumeration<java.net.InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof java.net.Inet6Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("getLocalIpv6 failed", e);
        }
        return "::0";
    }


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
