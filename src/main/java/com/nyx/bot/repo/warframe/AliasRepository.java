package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Warframe别名
 */
@Repository
public interface AliasRepository extends JpaRepository<Alias, Long>, JpaSpecificationExecutor<Alias> {
}
