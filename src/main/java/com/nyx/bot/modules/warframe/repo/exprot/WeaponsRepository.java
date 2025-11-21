package com.nyx.bot.modules.warframe.repo.exprot;

import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 武器数据
 */
@Repository
public interface WeaponsRepository extends JpaRepository<Weapons, String>, JpaSpecificationExecutor<Weapons> {
    @Query("SELECT w FROM Weapons w WHERE w.name LIKE %:name%")
    List<Weapons> findByNameContaining(@Param("name") String name);

    Optional<Weapons> findByName(String name);

    Optional<Weapons> findByEnglishName(String englishName);
}
