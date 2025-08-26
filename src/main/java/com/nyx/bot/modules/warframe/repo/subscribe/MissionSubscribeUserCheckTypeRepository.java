package com.nyx.bot.modules.warframe.repo.subscribe;

import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionSubscribeUserCheckTypeRepository extends JpaRepository<MissionSubscribeUserCheckType, Long>, JpaSpecificationExecutor<MissionSubscribeUserCheckType> {

    @Query("""
            SELECT t FROM MissionSubscribeUserCheckType t WHERE t.subscribe = :type
            AND
            (t.missionTypeEnum = :missionType OR :missionType IS NULL)
            AND
            (t.tierNum = :tier OR :tier IS NULL)
            """)
    List<MissionSubscribeUserCheckType> findMatching(
            @Param("type") SubscribeEnums type,
            @Param("missionType") WarframeMissionTypeEnum missionType,
            @Param("tier") Integer tier);

    @Query("""
            SELECT t FROM MissionSubscribeUserCheckType t WHERE t.subscribeUser.id = :id
            """)
    Page<MissionSubscribeUserCheckType> findAllBySUBU_ID(Long id, Pageable pageable);
}
