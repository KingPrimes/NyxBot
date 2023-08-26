package com.nyx.bot.repo;

import com.nyx.bot.entity.BotAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotAdminRepository extends JpaRepository<BotAdmin,Long> {
    BotAdmin findByAdminUid(Long adminUid);
}
