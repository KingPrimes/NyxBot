package com.nyx.bot.modules.bot.repo;

import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.bot.entity.BotAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotAdminRepository extends JpaRepository<BotAdmin, Long>, JpaSpecificationExecutor<BotAdmin> {
    Optional<BotAdmin> findByAdminUid(Long adminUid);

    @Query("select b from BotAdmin b where (b.permissions = :permissions)")
    Optional<BotAdmin> findByPermissions(PermissionsEnums permissions);

    @Query("select b from BotAdmin b where (:botUid is null or b.botUid = :botUid)")
    Page<BotAdmin> findAllByBotUid(Long botUid, Pageable pageable);
}
