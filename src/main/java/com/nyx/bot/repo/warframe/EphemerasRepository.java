package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Ephemeras;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EphemerasRepository extends JpaRepository<Ephemeras, Long>, JpaSpecificationExecutor<Ephemeras> {
    /**
     * 添加数据
     */
    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO EPHEMERAS(animation, element, id, icon, icon_format, item_name, thumb, url_name) VALUES (:#{#es.animation},:#{#es.element},:#{#es.id},:#{#es.icon},:#{#es.iconFormat},:#{#es.itemName},:#{#es.thumb},:#{#es.urlName})", nativeQuery = true)
    int addEphemeras(@Param("es") Ephemeras ephemeras);


    @Query(value = "select max(EID) from EPHEMERAS", nativeQuery = true)
    int queryMaxId();

    @Query("select e from Ephemeras e where (:itemName is null or Lower(e.itemName) like Lower(concat('%', :itemName, '%')))")
    Page<Ephemeras> findAllPageable(String itemName, Pageable pageable);

}
