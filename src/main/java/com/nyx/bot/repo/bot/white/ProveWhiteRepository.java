package com.nyx.bot.repo.bot.white;

import com.nyx.bot.entity.bot.white.ProveWhite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveWhiteRepository extends JpaRepository<ProveWhite, Long>, JpaSpecificationExecutor<ProveWhite> {

    /**
     * 根据QQ查询
     *
     * @param prove QQ账号
     */
    Optional<ProveWhite> findByProve(Long prove);


    @Query("select p from ProveWhite p where (:prove is null or p.prove = :prove)")
    Page<ProveWhite> findAllPageable(Long prove, Pageable pageable);
}
