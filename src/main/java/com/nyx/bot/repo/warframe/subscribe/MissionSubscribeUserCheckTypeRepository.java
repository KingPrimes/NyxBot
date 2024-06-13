package com.nyx.bot.repo.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSubscribeUserCheckTypeRepository extends JpaRepository<MissionSubscribeUserCheckType, Long>, JpaSpecificationExecutor<MissionSubscribeUserCheckType> {
}
