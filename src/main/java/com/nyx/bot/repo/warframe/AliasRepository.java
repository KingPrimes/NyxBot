package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Warframe别名
 */
@Repository
public interface AliasRepository extends JpaRepository<Alias,Long> {

    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO ALIAS(id,alias_us_en, alias_zh_cn) VALUES (:#{#alias.id},:#{#alias.aliasUsEn},:#{#alias.aliasZhCn})",nativeQuery = true)
    Integer addAlias(Alias alias);


    @Query(value = "select max(ID) from ALIAS",nativeQuery = true)
    Integer queryMaxId();
}
