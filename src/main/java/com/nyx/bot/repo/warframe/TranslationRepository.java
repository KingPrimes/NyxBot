package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Translation;
import jakarta.annotation.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

@Resource
public interface TranslationRepository extends JpaRepository<Translation, Long>, JpaSpecificationExecutor<Translation> {
    Translation findByEn(String en);

    List<Translation> findByEnLike(String en);

    /**
     * 查询最大ID的数据
     */
    Translation findTopByOrderByIdDesc();
}
