package com.nyx.bot.repo.sys;

import com.nyx.bot.entity.sys.LogInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 日志信息
 */
@Repository
public interface LogInfoRepository extends JpaRepository<LogInfo, Long>, JpaSpecificationExecutor<LogInfo> {
}
