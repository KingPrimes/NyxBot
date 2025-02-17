package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.config.TokenKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenKeysRepository extends JpaRepository<TokenKeys, Long> {
}
