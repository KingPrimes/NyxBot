package com.nyx.bot.repo.bot.black;

import com.nyx.bot.entity.bot.black.GroupBlack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBlackRepository extends JpaRepository<GroupBlack, Long>, JpaSpecificationExecutor<GroupBlack> {
    GroupBlack findByGroupUid(Long groupUid);

    /**
     * 分页查询
     */
    @Query("select g from GroupBlack g where (:groupUid is null or g.groupUid = :groupUid)")
    Page<GroupBlack> findAllPageable(Long groupUid, Pageable pageable);
}
