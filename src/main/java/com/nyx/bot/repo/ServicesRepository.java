package com.nyx.bot.repo;

import com.nyx.bot.entity.Services;
import com.nyx.bot.enums.ServicesEnums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicesRepository extends JpaRepository<Services, Long> {
    Services findByService(ServicesEnums enums);
}
