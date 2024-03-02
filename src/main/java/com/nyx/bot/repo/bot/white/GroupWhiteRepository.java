package com.nyx.bot.repo.bot.white;

import com.nyx.bot.entity.bot.white.GroupWhite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupWhiteRepository extends JpaRepository<GroupWhite, Long>, JpaSpecificationExecutor<GroupWhite> {
    /**
     * 根据 群号 查询
     *
     * @param group 群号
     */
    GroupWhite findByGroupUid(Long group);
}
