package com.nyx.bot.utils.gitutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * CDN 版本标签解析器，获取 DataSource 仓库最新 tag 并用其构建 CDN URL。
 * 标签获取通过 {@link HttpUtils#sendGet} 自带缓存（Cache2k，120 秒 TTL）。
 */
@Slf4j
public class CdnTagResolver {

    private static final String GITHUB_TAGS_URL = "https://api.github.com/repos/KingPrimes/DataSource/tags";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * CDN 数据源基础 URL 列表，jsDelivr 末尾带 @ 用于拼接版本号，kingprimes.top 直接拼接路径。
     */
    private static final List<String> DATA_SOURCE_BASE = List.of(
            "https://testingcf.jsdelivr.net/gh/KingPrimes/DataSource@",
            "https://jsd.onmicrosoft.cn/gh/KingPrimes/DataSource@",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource@",
            "https://kingprimes.top"
    );

    /**
     * 获取 DataSource 仓库最新 tag，HTTP 层缓存 120 秒。
     * 失败时回退到 "latest"。
     */
    public static String getLatestTag() {
        try {
            HttpUtils.Body body = HttpUtils.sendGet(GITHUB_TAGS_URL, "", null, 120);
            if (body.is2xxSuccessful()) {
                JsonNode root = mapper.readTree(body.body());
                if (root.isArray() && !root.isEmpty()) {
                    String tag = root.get(0).get("name").asText();
                    log.info("获取到 DataSource 最新版本: {}", tag);
                    return tag;
                }
            }
            log.warn("获取最新版本号失败，HTTP {}: {}", body.code(), body.body());
        } catch (Exception e) {
            log.error("获取最新版本号失败: {}", e.getMessage());
        }
        log.warn("回退到 latest");
        return "latest";
    }

    /**
     * 根据路径后缀构建完整的 CDN 数据源 URL 列表。
     * jsDelivr URL 拼接版本号，kingprimes.top 直接拼接路径。
     *
     * @param path 文件路径，如 "warframe/alias.json"
     */
    public static List<String> buildUrls(String path) {
        String tag = getLatestTag();
        return DATA_SOURCE_BASE.stream()
                .map(base -> base.endsWith("@") ? base + tag + "/" + path : base + "/" + path)
                .toList();
    }
}
