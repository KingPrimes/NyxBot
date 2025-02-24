package com.nyx.bot.repo.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.enums.SubscribeEnums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionSubscribeRepository extends JpaRepository<MissionSubscribe, Long>, JpaSpecificationExecutor<MissionSubscribe> {


    /**
     * @param subGroup 群号
     * @param pageable 分页规则
     * @return 分页数据
     */
    @Query("select m from MissionSubscribe m where (:subGroup is null or m.subGroup = :subGroup)")
    Page<MissionSubscribe> findAllPageable(Long subGroup, Pageable pageable);


    @EntityGraph(attributePaths = {
            "users",
            "users.checkTypes"
    })
    @Query("""
            SELECT s FROM MissionSubscribe s
            JOIN s.users u
            JOIN u.checkTypes t
            WHERE t.subscribe = :type
            """)
    List<MissionSubscribe> findSubscriptions(@Param("type") SubscribeEnums type);

    @EntityGraph(attributePaths = {"users", "users.checkTypes"})
    Optional<MissionSubscribe> findBySubGroup(Long subGroup);

}
