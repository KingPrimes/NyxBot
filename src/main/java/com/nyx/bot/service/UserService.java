package com.nyx.bot.service;

import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户登录
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Resource
    SysUserRepository sysUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SysUser> user = sysUserRepository.findSysUsersByUserName(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(I18nUtils.message("auth.error.NamePassword"));
        }
        SysUser sysUser = user.get();
        String role = sysUser.getUserId() != null && sysUser.getUserId() == 1L ? "ROLE_ADMIN" : "ROLE_USER";
        return User
                .withUsername(sysUser.getUserName())
                .password(sysUser.getPassword())
                .authorities(role)
                .build();
    }
}
