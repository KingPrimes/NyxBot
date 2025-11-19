package com.nyx.bot.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 插件配置实体类
 */
@Data
@Entity
@Table
@JsonView(Views.View.class)
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
