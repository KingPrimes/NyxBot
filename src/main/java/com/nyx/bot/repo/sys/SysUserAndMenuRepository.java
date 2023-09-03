package com.nyx.bot.repo.sys;

import com.nyx.bot.entity.sys.SysUserAndMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserAndMenuRepository extends JpaRepository<SysUserAndMenu, Long>, JpaSpecificationExecutor<SysMenuRepository> {

    @Query(value = "select * from SYS_USER_AND_MENU where USER_ID = :#{#userId}", nativeQuery = true)
    List<SysUserAndMenu> findByUserId(Long userId);

    SysUserAndMenu findByMenuId(Long menuId);

}
