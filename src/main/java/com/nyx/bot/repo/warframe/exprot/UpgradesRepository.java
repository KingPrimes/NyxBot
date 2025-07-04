package com.nyx.bot.repo.warframe.exprot;

import com.nyx.bot.entity.warframe.exprot.Upgrades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 外观
 */
@Repository
public interface UpgradesRepository extends JpaRepository<Upgrades, String>, JpaSpecificationExecutor<Upgrades> {
}
