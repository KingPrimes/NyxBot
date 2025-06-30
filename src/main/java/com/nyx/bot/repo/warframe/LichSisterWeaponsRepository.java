package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.LichSisterWeapons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSisterWeaponsRepository extends JpaRepository<LichSisterWeapons, String>, JpaSpecificationExecutor<LichSisterWeapons> {

}
