package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Translation;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

@Resource
public interface TranslationRepository extends JpaRepository<Translation, Long>, JpaSpecificationExecutor<Translation>, PagingAndSortingRepository<Translation, Long> {
    Translation findByEn(String en);

    List<Translation> findByEnLike(String en);

    /**
     * 查询最大ID的数据
     */
    Translation findTopByOrderByIdDesc();

    Translation findByCn(String cn);

    @Query("select t from Translation t where (:cn is null or LOWER(t.cn) like LOWER(CONCAT('%',:cn,'%'))) and (:isPrime is null or t.isPrime = :isPrime) and (:isSet is null or t.isSet = :isSet)")
    Page<Translation> findAllPageable(String cn, Boolean isPrime, Boolean isSet, Pageable pageable);
}
