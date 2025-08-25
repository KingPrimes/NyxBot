package com.nyx.bot.modules.warframe.repo.exprot;

import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 武器数据
 */
@Repository
public interface WeaponsRepository extends JpaRepository<Weapons, String>, JpaSpecificationExecutor<Weapons> {
}
