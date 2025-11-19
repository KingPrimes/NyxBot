package com.nyx.bot.repo;

import com.nyx.bot.entity.PluginConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 插件配置数据访问仓库
 */
@Repository
public interface PluginConfigRepository extends JpaRepository<PluginConfig, Long> {
    /**
     * 根据配置键名查找配置
     * @param pluginName 插件名称
     * @return 插件配置
     */
    Optional<PluginConfig> findByPluginName(String pluginName);
}
