package com.nyx.bot.repo.sys;

import com.nyx.bot.entity.sys.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {
    SysMenu findByParentId(Long parentId);

    SysMenu findByMenuName(String menuName);
}
