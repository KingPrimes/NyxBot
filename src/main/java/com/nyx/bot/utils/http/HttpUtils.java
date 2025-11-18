package com.nyx.bot.utils.http;

import com.nyx.bot.enums.MarketFormEnums;
import com.nyx.bot.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.concurrent.CompletableFuture;

/**
 * Http请求工具类</br>
 * 对{@link RestTemplate}进行封装，提供更方便的使用方式
 *
 * @author KingPrimes
 */
@Slf4j
@SuppressWarnings("unused")
public class HttpUtils {

    /**
     * 默认的请求头
     */
    public static final HttpHeaders headers;
    /**
     * 默认的 RestTemplate
     */
    private static final RestTemplate client;
    /**
     * 请求超时时间
     */
    private static final int CONNECT_TIMEOUT = 60000;
    /**
     * 读取超时
     */
    private static final int READ_TIMEOUT = 240000;
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
        headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "*/*");
        headers.add(HttpHeaders.CONNECTION, "keep-alive");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36 Edg/142.0.0.0");
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
        return sendGet(url, param, headers);
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
        try {
            String urlNameString = url;
            if (!param.isEmpty()) {
                urlNameString = url + "?" + param;
            }

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = getRestTemplateWithoutProxyIfNeeded(url).exchange(urlNameString, HttpMethod.GET, entity, String.class);

            return new Body(
                    response.getBody(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    url);
        } catch (RestClientException e) {
            log.error("sendGet", e);
            return new Body(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Http Get请求 Market 交易
     *
     * @param url 链接
     * @return 响应结果
     */
    public static Body marketSendGet(String url) {
        return marketSendGet(url, "", MarketFormEnums.PC);
    }

    /**
     * Http Get请求 Market 交易
     *
     * @param url   链接
     * @param param 参数
     * @return 响应结果
     */
    public static Body marketSendGet(String url, String param) {
        return marketSendGet(url, param, MarketFormEnums.PC);
    }

    /**
     * Http Get请求 Market 交易
     *
     * @param url   链接
     * @param param 参数
     * @param form  平台
     * @return 响应结果
     */
    public static Body marketSendGet(String url, String param, MarketFormEnums form) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.add("Language", "zh-hans");
        headers.add("Platform", form.getForm());
        headers.add("Pragma", "no-cache");
        headers.add("Crossplay", "true");
        return sendGet(url, param, headers);
    }


    /**
     * Http Post请求
     *
     * @param url  请求地址
     * @param json 请求参数
     * @return 响应结果
     */
    public static Body sendPost(String url, String json) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = getRestTemplateWithoutProxyIfNeeded(url).exchange(url, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Response Code Is Not Successful code:{},message:{}", response.getStatusCode().value(), response.getBody());
                return new Body(response.getStatusCode());
            }

            return getBody(response, url);
        } catch (RestClientException e) {
            log.warn("sendPost", e);
            return new Body(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据URL网址获取文件
     *
     * @param url  - url
     * @param path - 文件输出路径
     */
    public static Boolean sendGetForFile(String url, String path) {
        // 用于下载完成返回标志符
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        File outputFile = new File(path);
        // 若目录不存在,创建目录
        FileUtils.createDir(outputFile);

        try {
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Resource> response = getRestTemplateWithoutProxyIfNeeded(url).exchange(url, HttpMethod.GET, entity, Resource.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                try (
                        InputStream in = response.getBody().getInputStream();
                        FileOutputStream out = new FileOutputStream(outputFile)) {
                    long fileSize = response.getHeaders().getContentLength();
                    long downloaded = 0;
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloaded += bytesRead;
                        // 输出进度
                        printDownloadProgress(fileSize, downloaded);
                    }
                    future.complete(true);
                } catch (IOException e) {
                    log.error("文件写入失败: {}", e.getMessage());
                    future.complete(false);
                    future.completeExceptionally(e);
                }
            } else {
                log.warn("文件下载： code：{}，headers：{}，message：{}", response.getStatusCode().value(), response.getHeaders(), response.getBody());
                future.complete(false);
            }
        } catch (RestClientException e) {
            log.warn("sendGetForFile 出现异常 请求Headers:{}", headers, e);
            future.complete(false);
            future.completeExceptionally(e);
        }
        return future.join();
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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Accept-Encoding", "application/octet-stream");
            HttpEntity<String> request = new HttpEntity<>(json, headers);
            ResponseEntity<?> response = getRestTemplateWithoutProxyIfNeeded(url).exchange(url, HttpMethod.POST, request, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("请求失败 code:{}, message:{}", response.getStatusCode().value(), response.getBody());
                return new Body(response.getStatusCode());
            }
            return new Body("", response.getStatusCode(), response.getHeaders(), url, (byte[]) response.getBody());
        } catch (RestClientException e) {
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

    private static Body getBody(ResponseEntity<String> response, String url) {
        return new Body(response.getBody(),
                response.getStatusCode(),
                response.getHeaders(),
                url,
                null);
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
