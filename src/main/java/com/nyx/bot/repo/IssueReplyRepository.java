package com.nyx.bot.repo;

import com.nyx.bot.entity.bot.IssueReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueReplyRepository extends JpaRepository<IssueReply, Long>, JpaSpecificationExecutor<IssueReply> {
}
