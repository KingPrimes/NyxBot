package com.nyx.bot.modules.system.repo;

import com.nyx.bot.modules.system.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统管理员
 * Jpa 操作数据源 接口
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findSysUsersByUserName(String userName);

}
