package com.nyx.bot.repo.warframe;

import com.nyx.bot.entity.warframe.RivenTion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 紫卡词条参数
 */
@Repository
public interface RivenTionRepository extends JpaRepository<RivenTion, Long> {
    Optional<RivenTion> findByEffect(String stat);

    Optional<RivenTion> findByUrlName(String urlName);
}
