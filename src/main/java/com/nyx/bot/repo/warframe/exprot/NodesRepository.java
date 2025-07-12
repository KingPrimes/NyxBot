package com.nyx.bot.repo.warframe.exprot;

import com.nyx.bot.entity.warframe.exprot.Nodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 节点
 */
@Repository
public interface NodesRepository extends JpaRepository<Nodes, String>, JpaSpecificationExecutor<Nodes> {
}
