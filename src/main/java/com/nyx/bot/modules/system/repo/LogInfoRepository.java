package com.nyx.bot.modules.system.repo;

import com.nyx.bot.enums.LogTitleEnum;
import com.nyx.bot.modules.system.entity.LogInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 日志信息
 */
@Repository
public interface LogInfoRepository extends JpaRepository<LogInfo, Long>, JpaSpecificationExecutor<LogInfo> {

    @Query("select l from LogInfo l where  (l.title = :title or :title is null) and (:code is null or l.code = :code) and (:groupUid is null or l.groupUid = :groupUid)")
    Page<LogInfo> findAllPageable(LogTitleEnum title, String code, Long groupUid, Pageable pageable);

}
