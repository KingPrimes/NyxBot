package com.nyx.bot.repo.bot.black;

import com.nyx.bot.entity.bot.black.ProveBlack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveBlackRepository extends JpaRepository<ProveBlack, Long>, JpaSpecificationExecutor<ProveBlack> {
    Optional<ProveBlack> findByProveUid(Long prove);

    @Query("select p from ProveBlack p where (:proveUid is null or p.proveUid = :proveUid)")
    Page<ProveBlack> findAllPageable(Long proveUid, Pageable pageable);
}
