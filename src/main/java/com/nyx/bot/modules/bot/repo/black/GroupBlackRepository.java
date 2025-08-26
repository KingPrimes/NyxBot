package com.nyx.bot.modules.bot.repo.black;

import com.nyx.bot.modules.bot.entity.black.GroupBlack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupBlackRepository extends JpaRepository<GroupBlack, Long>, JpaSpecificationExecutor<GroupBlack> {
    Optional<GroupBlack> findByGroupUid(Long groupUid);

    /**
     * 分页查询
     */
    @Query("select g from GroupBlack g where (:groupUid is null or g.groupUid = :groupUid)")
    Page<GroupBlack> findAllPageable(Long groupUid, Pageable pageable);
}
