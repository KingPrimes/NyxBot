package com.nyx.bot.runner;

import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class Runners {

    private final SysUserRepository userRepository;
    private final WarframeDataSource dataSource;
    @Value("${test.isTest}")
    Boolean test;

    public Runners(SysUserRepository userRepository, WarframeDataSource dataSource) {
        this.userRepository = userRepository;
        this.dataSource = dataSource;
    }

    //程序启动完成后初始化Web系统用户
    @Bean
    public ApplicationRunner sysUser() {
        return args -> AsyncUtils.me().execute(() -> {
            SysUser user = new SysUser();
            user.setUserId(1L);
            // 获取随机字母不包含特殊字符
            String name = StringUtils.getRandomLetters(6);
            user.setUserName(name);
            String password = StringUtils.getRandomLetters(8);
            // {bcrypt} 密码加密方式
            user.setPassword(SpringUtils.getBean(PasswordEncoder.class).encode(password));
            List<SysUser> all = userRepository.findAll();
            if (all.isEmpty()) {
                log.info("默认账号：{} 随机密码：{} \t 请修改随机密码，或保存好随机密码！", name, password);
                userRepository.save(user);
            }
        }, AsyncBeanName.SERVICE);
    }


    /**
     * 初始化服务
     */
    @Bean
    public ApplicationRunner service() {
        return args -> {
            if (!test) {
                //程序启动之后获取WarframeDataSource
                dataSource.init();
            }
        };
    }

    //程序启动完成启动浏览器
    @Bean
    public ApplicationRunner browser() {
        return args -> {
            if (!test) {
                IoUtils.index();
            }
        };
    }
}