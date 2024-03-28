package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Weapons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WeaponsRepository extends JpaRepository<Weapons, String>, JpaSpecificationExecutor<Weapons> {

}
