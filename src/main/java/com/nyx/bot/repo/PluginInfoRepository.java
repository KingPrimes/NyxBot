package com.nyx.bot.repo;

import com.nyx.bot.entity.PluginInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 插件信息数据访问仓库。
 * <p>
 * 存储已安装插件的元数据，支持名称查询和全量列表获取。
 * </p>
 *
 * @author KingPrimes
 */
@Repository
public interface PluginInfoRepository extends JpaRepository<PluginInfo, Long> {

    /**
     * 根据插件名称查询已安装的插件信息。
     *
     * @param pluginName 插件唯一标识名
     * @return 插件信息（可能为空）
     */
    Optional<PluginInfo> findByPluginName(String pluginName);
}
