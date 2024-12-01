package com.nyx.bot.repo;

import com.nyx.bot.entity.bot.BotAdmin;
import com.nyx.bot.enums.PermissionsEnums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BotAdminRepository extends JpaRepository<BotAdmin, Long>, JpaSpecificationExecutor<BotAdmin> {
    BotAdmin findByAdminUid(Long adminUid);

    @Query("select b from BotAdmin b where (b.permissions = :permissions)")
    BotAdmin findByPermissions(PermissionsEnums permissions);
}
