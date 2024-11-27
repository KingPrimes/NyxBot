package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Alias;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Warframe别名
 */
@Repository
public interface AliasRepository extends JpaRepository<Alias, Long>, JpaSpecificationExecutor<Alias> {


    @Query("select a from Alias a where (:cn is null or LOWER(a.cn) like LOWER(CONCAT('%', :cn, '%')))")
    Page<Alias> findByLikeCn(String cn, Pageable pageable);


    Alias findByCn(String cn);

    @Query("select a from Alias a where a.cn =:cn and a.en = :en")
    Alias findByCnAndEn(String cn, String en);
}
