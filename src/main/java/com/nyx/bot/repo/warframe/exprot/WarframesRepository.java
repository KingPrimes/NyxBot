package com.nyx.bot.repo.warframe.exprot;

import com.nyx.bot.entity.warframe.exprot.Warframes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 武器数据
 */
@Repository
public interface WarframesRepository extends JpaRepository<Warframes, String>, JpaSpecificationExecutor<Warframes> {
}
