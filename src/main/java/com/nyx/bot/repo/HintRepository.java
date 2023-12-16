package com.nyx.bot.repo;

import com.nyx.bot.entity.Hint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface HintRepository extends JpaRepository<Hint, Long> {

    @Transactional
    @Modifying
    @Query(value = "INSERT into HINT(hint)values(:#{#h.hint})", nativeQuery = true)
    int addHint(@Param("h") Hint hint);

    @Query(value = "SELECT * FROM HINT order by rand() limit 1", nativeQuery = true)
    Hint randOne();
}
