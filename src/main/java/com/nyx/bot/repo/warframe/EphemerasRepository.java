package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Ephemeras;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EphemerasRepository extends JpaRepository<Ephemeras, String>, JpaSpecificationExecutor<Ephemeras> {

    @Query("select e from Ephemeras e where (:itemName is null or Lower(e.itemName) like Lower(concat('%', :itemName, '%')))")
    Page<Ephemeras> findAllPageable(String itemName, Pageable pageable);

}
