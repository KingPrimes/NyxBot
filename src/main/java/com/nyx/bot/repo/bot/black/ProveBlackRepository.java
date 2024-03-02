package com.nyx.bot.repo.bot.black;

import com.nyx.bot.entity.bot.black.ProveBlack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveBlackRepository extends JpaRepository<ProveBlack, Long>, JpaSpecificationExecutor<ProveBlack> {
    ProveBlack findByProve(Long prove);
}
