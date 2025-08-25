package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.NotTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NotTranslationRepository extends JpaRepository<NotTranslation, Long>, JpaSpecificationExecutor<NotTranslation> {
    NotTranslation findByNotTranslation(String noTranslation);
}
