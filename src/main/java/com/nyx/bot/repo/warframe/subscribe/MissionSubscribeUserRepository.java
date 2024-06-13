package com.nyx.bot.repo.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSubscribeUserRepository extends JpaRepository<MissionSubscribeUser, Long>, JpaSpecificationExecutor<MissionSubscribeUser> {
}
