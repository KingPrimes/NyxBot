package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Weapons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WeaponsRepository extends JpaRepository<Weapons, Long>, JpaSpecificationExecutor<Weapons> {
    /**
     * 添加数据
     */
    @Transactional
    @Modifying
    @Query(value = "INSERT IGNORE INTO WEAPONS(icon, icon_for_mat, item_name, thumb, url_name, weapon_id) VALUES (:#{#weapon.icon},:#{#weapon.iconForMat},:#{#weapon.itemName},:#{#weapon.thumb},:#{#weapon.urlName},:#{#weapon.weaponId})", nativeQuery = true)
    Integer addWeapons(@Param("weapon") Weapons weapons);

    @Query(value = "select max(id) from Weapons")
    Integer queryMaxId();

    Weapons findWeaponsByWeaponId(String id);

}
