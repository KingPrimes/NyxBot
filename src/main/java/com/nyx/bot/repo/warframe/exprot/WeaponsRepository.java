package com.nyx.bot.repo.warframe.exprot;

import com.nyx.bot.entity.warframe.exprot.Weapons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 武器数据
 */
@Repository
public interface WeaponsRepository extends JpaRepository<Weapons, String>, JpaSpecificationExecutor<Weapons> {
}
