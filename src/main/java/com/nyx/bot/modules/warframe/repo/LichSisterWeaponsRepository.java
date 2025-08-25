package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSisterWeaponsRepository extends JpaRepository<LichSisterWeapons, String>, JpaSpecificationExecutor<LichSisterWeapons> {

}
