package com.nyx.bot.modules.bot.repo.white;

import com.nyx.bot.modules.bot.entity.white.ProveWhite;
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
     * @param proveUid QQ账号
     */
    Optional<ProveWhite> findByProveUid(Long proveUid);


    @Query("select p from ProveWhite p where (:proveUid is null or p.proveUid = :proveUid)")
    Page<ProveWhite> findAllPageable(Long proveUid, Pageable pageable);
}
