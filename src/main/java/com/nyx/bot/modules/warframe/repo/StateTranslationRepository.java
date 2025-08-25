package com.nyx.bot.modules.warframe.repo;

import com.nyx.bot.modules.warframe.entity.StateTranslation;
import jakarta.annotation.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Resource
public interface StateTranslationRepository extends JpaRepository<StateTranslation, String>, JpaSpecificationExecutor<StateTranslation> {

    @Query("SELECT st FROM StateTranslation st WHERE RIGHT(st.uniqueName,LENGTH(:uniqueName)) = :uniqueName")
    Optional<StateTranslation> findByUniqueName(String uniqueName);
}
