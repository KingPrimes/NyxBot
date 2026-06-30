package com.nyx.bot.pluginmarket;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 插件市场索引中的单个插件条目。
 * <p>
 * 对应 {@code plugin-index.json} 中 {@code plugins} 字典的每个 value。
 * 包含插件展示信息、标签、所有版本的发布信息。
 * </p>
 *
 * @author KingPrimes
 */
@Data
public class PluginIndexEntry {

    /** 插件唯一标识名（对应 key） */
    String name;

    /** 展示名称（中文/英文） */
    String displayName;

    /** 插件描述 */
    String description;

    /** 作者 GitHub 用户名 */
    String author;

    /** 类型：jar / native */
    String type;

    /** 源代码仓库（格式：owner/repo） */
    String repository;

    /** 许可证标识（如 MIT, GPL-3.0） */
    String license;

    /** 项目主页 URL */
    String homepage;

    /** 图标图片 URL */
    String iconUrl;

    /** 标签列表（如 ["draw","native","jna"]） */
    List<String> tags;

    /** 版本映射，key 为语义版本号（如 "2.0.0"），value 为版本详情 */
    Map<String, PluginVersionEntry> versions;
}
