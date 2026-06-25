package com.nyx.bot.repo;

import com.nyx.bot.entity.PluginInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 插件信息数据访问仓库。
 * <p>
 * 当前仅预建接口，业务逻辑在后续阶段实现。
 * </p>
 *
 * @author KingPrimes
 */
@Repository
public interface PluginInfoRepository extends JpaRepository<PluginInfo, Long> {
}
