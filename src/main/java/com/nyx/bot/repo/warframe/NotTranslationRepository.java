package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.NotTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotTranslationRepository extends JpaRepository<NotTranslation, Long>, JpaSpecificationExecutor<NotTranslation>, PagingAndSortingRepository<NotTranslation, Long> {
    NotTranslation findByNotTranslation(String noTranslation);
}
