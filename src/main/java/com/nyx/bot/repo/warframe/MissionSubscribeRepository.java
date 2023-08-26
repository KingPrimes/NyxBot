package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSubscribeRepository extends JpaRepository<MissionSubscribe,Long> {


}
