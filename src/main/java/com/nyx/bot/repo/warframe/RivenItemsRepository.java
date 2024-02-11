package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RivenItemsRepository extends JpaRepository<RivenItems, Long>, JpaSpecificationExecutor<RivenItems>, PagingAndSortingRepository<RivenItems, Long> {
    @Query(value = "select max(id) from RivenTrend")
    Long queryMaxId();

    RivenItems findByRivenId(String rivenId);
}
