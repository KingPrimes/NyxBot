package com.nyx.bot.pluginmarket;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 插件更新检查结果 DTO。
 * <p>
 * 用于在 Web UI 中展示哪些插件可以升级。
 * </p>
 *
 * @author KingPrimes
 */
@Data
@AllArgsConstructor
public class UpdateInfo {

    /** 插件唯一标识名 */
    String pluginName;

    /** 展示名称 */
    String displayName;

    /** 当前已安装的版本 */
    String currentVersion;

    /** 市场中可用的最新版本 */
    String latestVersion;

    /** 是否有可用更新 */
    boolean hasUpdate;

    /** 最新版本的下载 URL */
    String downloadUrl;

    /** 最新版本的文件大小（字节） */
    Long fileSize;

    /** 版本发布说明 */
    String releaseNotes;
}
