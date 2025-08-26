package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.RivenTionAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 紫卡词条别名
 */
@Repository
public interface RivenTionAliasRepository extends JpaRepository<RivenTionAlias, Long> {
    Optional<RivenTionAlias> findByCn(String stat);
}
