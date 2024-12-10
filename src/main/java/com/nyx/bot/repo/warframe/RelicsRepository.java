package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.Relics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelicsRepository extends JpaRepository<Relics, String>, JpaSpecificationExecutor<Relics>, PagingAndSortingRepository<Relics, String> {

    List<Relics> findByRelicName(String name);

    List<Relics> findByTier(String tier);
}
