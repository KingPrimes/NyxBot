package com.nyx.bot.repo.bot.white;

import com.nyx.bot.entity.bot.white.GroupWhite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupWhiteRepository extends JpaRepository<GroupWhite, Long>, JpaSpecificationExecutor<GroupWhite> {
    /**
     * 根据 群号 查询
     *
     * @param group 群号
     */
    Optional<GroupWhite> findByGroupUid(Long group);

    @Query("select g from GroupWhite g where (:groupUid is null or g.groupUid = :groupUid)")
    Page<GroupWhite> findAllPageable(Long groupUid, Pageable pageable);
}
