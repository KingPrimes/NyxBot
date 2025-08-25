package com.nyx.bot.modules.warframe.repo.exprot;

import com.nyx.bot.modules.warframe.entity.exprot.Sentinels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 外观
 */
@Repository
public interface SentinelsRepository extends JpaRepository<Sentinels, String>, JpaSpecificationExecutor<Sentinels> {
}
