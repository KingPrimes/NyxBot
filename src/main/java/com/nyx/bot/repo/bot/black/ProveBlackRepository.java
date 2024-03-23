package com.nyx.bot.repo.bot.black;

import com.nyx.bot.entity.bot.black.ProveBlack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveBlackRepository extends JpaRepository<ProveBlack, Long>, JpaSpecificationExecutor<ProveBlack> {
    ProveBlack findByProve(Long prove);

    @Query("select p from ProveBlack p where (:prove is null or p.prove = :prove)")
    Page<ProveBlack> findAllPageable(Long prove, Pageable pageable);
}
