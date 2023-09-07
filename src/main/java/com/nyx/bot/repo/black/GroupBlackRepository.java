package com.nyx.bot.repo.black;

import com.nyx.bot.entity.bot.black.GroupBlack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBlackRepository extends JpaRepository<GroupBlack, Long>, JpaSpecificationExecutor<GroupBlack> {
    GroupBlack findByGroupUid(Long groupUid);
}
