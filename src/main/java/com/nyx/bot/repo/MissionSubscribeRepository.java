package com.nyx.bot.repo;

import com.nyx.bot.entity.MissionSubscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSubscribeRepository extends JpaRepository<MissionSubscribe,Long> {


}
