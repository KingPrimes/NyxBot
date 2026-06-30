package com.nyx.bot.pluginmarket;

import lombok.Data;

/**
 * 插件市场索引中的单个版本条目。
 * <p>
 * 对应 {@code plugin-index.json} 中 {@code versions} 字典的每个 value，
 * 包含特定版本的可下载包信息和校验数据。
 * </p>
 *
 * @author KingPrimes
 */
@Data
public class PluginVersionEntry {

    /** 插件 jar 文件的下载 URL（GitHub Release 直链） */
    String downloadUrl;

    /** 文件大小（字节） */
    Long fileSize;

    /** SHA-256 校验和（十六进制字符串） */
    String sha256;

    /** 最低 Java 版本要求（如 ">=21"） */
    String requires;

    /** 版本发布说明 */
    String releaseNotes;
}
