package com.nyx.bot.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 插件信息实体。
 * <p>
 * 存储已安装/从市场拉取的插件元数据，用于市场展示和版本管理。
 * 注意：当前仅预建表结构，业务逻辑在后续阶段实现。
 * </p>
 *
 * @author KingPrimes
 */
@Data
@Entity
@Table(name = "plugin_info")
public class PluginInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /** 插件唯一标识名（对应 plugin-index.json 中的 key） */
    @Column(unique = true, nullable = false)
    String pluginName;

    /** 展示名称 */
    String displayName;

    /** 当前已安装的版本号 */
    String version;

    /** 描述 */
    @Column(columnDefinition = "text")
    String description;

    /** 作者 */
    String author;

    /** 类型：jar / native */
    String type;

    /** 图标 URL */
    @Column(columnDefinition = "text")
    String iconUrl;

    /** 文件路径（plugin 目录下的实际路径） */
    String filePath;

    /** 文件大小 */
    Long fileSize;

    /** 下载 URL */
    @Column(columnDefinition = "text")
    String downloadUrl;

    /** 源代码仓库地址 */
    @Column(columnDefinition = "text")
    String repository;

    /** 许可证 */
    String license;

    /** 主页 */
    @Column(columnDefinition = "text")
    String homepage;

    /** 是否启用 */
    Boolean enabled;

    /** 标签（JSON 数组字符串，如 ["draw","native","jna"]） */
    String tags;

    /** 安装时间 */
    @CreationTimestamp
    LocalDateTime installAt;

    /** 更新时间 */
    @UpdateTimestamp
    LocalDateTime updatedAt;
}
