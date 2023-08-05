package com.nyx.bot.repo;

import com.nyx.bot.entity.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Warframe别名
 */
@Repository
public interface AliasRepository extends JpaRepository<Alias,Long> {
}
