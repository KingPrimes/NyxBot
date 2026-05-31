package com.nyx.bot.utils.http;

import com.nyx.bot.common.event.DownloadProgressEvent;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils.Body;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * HTTP 文件下载工具
 * 提供文件下载、进度显示（控制台 + SSE 事件）功能
 *
 * @author KingPrimes
 */
@Slf4j
public class HttpFileDownloader {

    /**
     * 根据URL网址获取文件
     *
     * @param url  - url
     * @param path - 文件输出路径
     */
    public static Boolean sendGetForFile(String url, String path) {
        File outputFile = new File(path);
        FileUtils.createDir(outputFile);
        return downloadFile(url, HttpMethod.GET, null, null, outputFile);
    }

    /**
     * 发送Post请求 获取文件
     *
     * @param url  - url
     * @param json Json格式的请求参数
     * @return Body 包含文件字节数组
     */
    public static Body sendPostForFile(String url, String json) {
        try {
            HttpHeaders h = new HttpHeaders(HttpUtils.getHeaders());
            h.setContentType(MediaType.APPLICATION_JSON);
            h.add("Accept-Encoding", "application/octet-stream");
            HttpEntity<@NonNull String> request = new HttpEntity<>(json, h);
            ResponseEntity<@NonNull Resource> response = HttpUtils.getRestTemplateForUrl(url)
                    .exchange(url, HttpMethod.POST, request, Resource.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("请求失败 code:{}, message:{}", response.getStatusCode().value(), response.getBody());
                return new Body(response.getStatusCode());
            }
            if (response.getBody() != null) {
                return new Body("", response.getStatusCode(), response.getHeaders(), url,
                        response.getBody().getContentAsByteArray());
            } else {
                return new Body("", response.getStatusCode(), response.getHeaders(), url,
                        null);
            }
        } catch (RestClientException | IOException e) {
            log.warn("请求异常", e);
            return new Body(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 下载文件（核心逻辑）
     */
    public static boolean downloadFile(String url, HttpMethod method, Object requestBody,
                                       HttpHeaders extraHeaders, File output) {
        HttpHeaders hers = new HttpHeaders(HttpUtils.getHeaders());
        hers.setContentType(MediaType.APPLICATION_JSON);
        hers.add("Accept-Encoding", "application/octet-stream");
        if (extraHeaders != null) hers.putAll(extraHeaders);

        HttpEntity<?> req = (requestBody != null)
                ? new HttpEntity<>(requestBody, hers)
                : new HttpEntity<>(hers);

        ResponseEntity<@NonNull Resource> response = HttpUtils.getRestTemplateForUrl(url)
                .exchange(url, method, req, Resource.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try (InputStream in = response.getBody().getInputStream();
                 FileOutputStream out = new FileOutputStream(output)) {

                long total = response.getHeaders().getContentLength();
                log.debug("开始下载文件: {} ({} bytes)", url, total);
                byte[] buf = new byte[1024];
                int n;
                long read = 0, lastEventTs = 0;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    read += n;
                    printProgress(total, read);
                    // 每 500ms 发布一次进度事件到 SSE
                    long now = System.currentTimeMillis();
                    if (now - lastEventTs >= 500) {
                        SpringUtils.publishEvent(
                                new DownloadProgressEvent(HttpUtils.class, url, read, total, false));
                        lastEventTs = now;
                    }
                }
                SpringUtils.publishEvent(
                        new DownloadProgressEvent(HttpUtils.class, url, read, total > 0 ? total : read, true));
                log.debug("文件下载完成: {} -> {}", url, output.getAbsolutePath());
                return true;
            } catch (Exception e) {
                log.error("downloadFile failed for {} Headers:{}", url, response.getHeaders(), e);
                return false;
            }
        }
        log.warn("downloadFile failed for {} Headers:{}", url, response.getHeaders());
        return false;
    }

    // ══════════════════════════════════════════════
    // 控制台进度条
    // ══════════════════════════════════════════════

    /**
     * 控制台进度条（\r 覆盖刷新，兼容 Win/Linux/macOS 终端）
     */
    private static void printProgress(long fileSize, long downloaded) {
        int barWidth = 30;
        long total = fileSize > 0 ? fileSize : Long.MAX_VALUE;
        int filled = (int) ((double) downloaded / total * barWidth);
        filled = Math.min(filled, barWidth);

        StringBuilder bar = new StringBuilder("\r ");
        bar.append(fileSize > 0 ? '┃' : '…');
        for (int i = 0; i < barWidth; i++) {
            if (i < filled) bar.append('█');
            else if (i == filled && fileSize > 0) bar.append('▓');
            else bar.append('░');
        }
        bar.append(fileSize > 0 ? '┃' : '…');

        double pct = total == Long.MAX_VALUE ? 0 : (double) downloaded / total * 100;
        bar.append(String.format(" %5.1f%%", Math.min(pct, 100)));

        bar.append("  ").append(formatSize(downloaded));
        if (fileSize > 0) {
            bar.append(" / ").append(formatSize(fileSize));
        }

        System.out.print(bar);
        if (fileSize > 0 && downloaded >= fileSize) {
            System.out.println();
        }
    }

    /**
     * 格式化字节大小为可读字符串
     */
    static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024;
        if (mb < 1024) return String.format("%.1f MB", mb);
        return String.format("%.2f GB", mb / 1024);
    }
}
