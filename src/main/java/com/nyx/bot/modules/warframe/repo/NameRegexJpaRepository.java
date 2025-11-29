package com.nyx.bot.modules.warframe.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface NameRegexJpaRepository<T,ID> extends JpaRepository<T,ID> {
    Optional<T> findByNameRegex(String name);
}
