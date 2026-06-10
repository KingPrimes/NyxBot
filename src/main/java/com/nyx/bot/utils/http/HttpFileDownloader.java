package com.nyx.bot.utils.http;

import com.nyx.bot.common.event.DownloadProgressEvent;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

/**
 * HTTP 文件下载工具
 * 基于 HttpUtils 封装的方法，不直接接触 HttpClient 生命周期
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
        return downloadFile(url, outputFile);
    }

    /**
     * 发送Post请求 获取文件
     *
     * @param url  - url
     * @param json Json格式的请求参数
     * @return Body 包含文件字节数组
     */
    public static HttpUtils.Body sendPostForFile(String url, String json) {
        return HttpUtils.sendPostForBytes(url, json);
    }

    /**
     * 下载文件（核心逻辑）
     * 使用 Java 21 HttpClient 的非阻塞 I/O + InputStream 流式下载，避免 pin 虚拟线程
     */
    public static boolean downloadFile(String url, File output) {
        try {
            HttpResponse<InputStream> response = HttpUtils.sendGetForStream(url);

            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body() != null) {
                try (InputStream in = response.body();
                     FileOutputStream out = new FileOutputStream(output)) {

                    long total = response.headers().firstValue("content-length")
                            .map(Long::parseLong).orElse(-1L);
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
                }
            }
            log.warn("downloadFile failed for {} statusCode:{}", url, response.statusCode());
            return false;
        } catch (IOException | InterruptedException e) {
            log.error("downloadFile failed for {}: {}", url, e.getMessage(), e);
            return false;
        }
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
