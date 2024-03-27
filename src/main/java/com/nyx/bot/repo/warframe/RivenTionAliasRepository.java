package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenTionAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 紫卡词条别名
 */
@Repository
public interface RivenTionAliasRepository extends JpaRepository<RivenTionAlias, Long> {
    RivenTionAlias findByCn(String stat);
}
