package com.nyx.bot.repo;

import com.nyx.bot.entity.Ephemeras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EphemerasRepository extends JpaRepository<Ephemeras,Long> {
    /**
     * 添加数据
     */
    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO EPHEMERAS(animation, element, ephemeras_id, icon, icon_format, item_name, thumb, url_name) VALUES (:#{#es.animation},:#{#es.element},:#{#es.ephemerasId},:#{#es.icon},:#{#es.iconFormat},:#{#es.itemName},:#{#es.thumb},:#{#es.urlName})",nativeQuery = true)
    int addEphemeras(@Param("es") Ephemeras ephemeras);


    @Query(value = "select max(ID) from EPHEMERAS",nativeQuery = true)
    int queryMaxId();
}
