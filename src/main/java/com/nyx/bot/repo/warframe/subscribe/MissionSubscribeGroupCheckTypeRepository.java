package com.nyx.bot.repo.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribeGroupCheckType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSubscribeGroupCheckTypeRepository extends JpaRepository<MissionSubscribeGroupCheckType, Long>, JpaSpecificationExecutor<MissionSubscribeGroupCheckType> {
}
