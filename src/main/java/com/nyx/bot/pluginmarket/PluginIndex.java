package com.nyx.bot.pluginmarket;

import lombok.Data;

import java.util.Map;

/**
 * 插件市场索引（对应 {@code plugin-index.json} 根节点）。
 * <p>
 * 从 KingPrimes/nyxbot-plugins 仓库的 {@code plugin-index.json} 反序列化而来，
 * 包含所有可用插件的元数据和版本信息。
 * </p>
 *
 * @author KingPrimes
 * @see PluginIndexEntry
 * @see PluginVersionEntry
 */
@Data
public class PluginIndex {

    /** JSON Schema 版本号 */
    String schemaVersion;

    /** 市场标识名称 */
    String marketplace;

    /** 索引更新时间（ISO 8601） */
    String updatedAt;

    /** 插件集合，key 为插件唯一标识名，value 为插件元数据 */
    Map<String, PluginIndexEntry> plugins;
}
