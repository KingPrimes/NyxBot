package com.nyx.bot.repo.bot.white;

import com.nyx.bot.entity.bot.white.ProveWhite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveWhiteRepository extends JpaRepository<ProveWhite, Long>, JpaSpecificationExecutor<ProveWhite> {

    /**
     * 根据QQ查询
     *
     * @param prove QQ账号
     */
    ProveWhite findByProve(Long prove);

}
