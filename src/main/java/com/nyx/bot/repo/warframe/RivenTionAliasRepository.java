package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenTionAlias;
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
