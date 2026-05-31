package com.nyx.bot.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 插件配置实体类
 */
@Data
@Entity
@Table
public class PluginConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * 插件名称
     */
    @Column(nullable = false)
    String pluginName;

}
