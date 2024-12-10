package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RelicsRewards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelicsRewardsRepository extends JpaRepository<RelicsRewards, Long>, JpaSpecificationExecutor<RelicsRewards>, PagingAndSortingRepository<RelicsRewards, Long> {

    List<RelicsRewards> findByItemName(String itemName);
}
