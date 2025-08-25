package com.nyx.bot.modules.warframe.repo.subscribe;

import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSubscribeUserRepository extends JpaRepository<MissionSubscribeUser, Long>, JpaSpecificationExecutor<MissionSubscribeUser> {

    @Query("""
                    select u from MissionSubscribeUser u where (u.missionSubscribe.id = :id)
            """)
    Page<MissionSubscribeUser> findAllBySUB_ID(Long id, Pageable pageable);
}
