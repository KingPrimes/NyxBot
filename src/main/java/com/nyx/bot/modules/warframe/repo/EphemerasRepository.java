package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.Ephemeras;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EphemerasRepository extends JpaRepository<Ephemeras, String>, JpaSpecificationExecutor<Ephemeras> {

    @Query("select e from Ephemeras e where (:itemName is null or Lower(e.name) like Lower(concat('%', :itemName, '%')))")
    Page<Ephemeras> findAllPageable(String itemName, Pageable pageable);

}
