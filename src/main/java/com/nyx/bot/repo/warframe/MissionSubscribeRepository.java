package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSubscribeRepository extends JpaRepository<MissionSubscribe, Long>, JpaSpecificationExecutor<MissionSubscribe> {


    @Query("select m from MissionSubscribe m where (:subGroup is null or m.subGroup = :subGroup)")
    Page<MissionSubscribe> findAllPageable(Long subGroup, Pageable pageable);

}
