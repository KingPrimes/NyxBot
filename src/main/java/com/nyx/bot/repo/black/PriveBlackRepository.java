package com.nyx.bot.repo.black;

import com.nyx.bot.entity.bot.black.PriveBlack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PriveBlackRepository extends JpaRepository<PriveBlack, Long>, JpaSpecificationExecutor<PriveBlack> {
    PriveBlack findByUserUid(Long userUid);
}
