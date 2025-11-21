package com.nyx.bot.service;

import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
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
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return user.map(sysUser -> User
                .withUsername(sysUser.getUserName())
                .password(sysUser.getPassword())
                .build()).orElse(null);

    }
}
