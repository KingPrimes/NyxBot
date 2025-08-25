package com.nyx.bot.service;

import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户登录
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired
    SysUserRepository sysUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserRepository.findSysUsersByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User
                .withUsername(user.getUserName())
                .password(user.getPassword())
                .build();
    }
}
