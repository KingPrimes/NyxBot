package com.nyx.bot.runner;

import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.sys.SysUser;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.plugin.warframe.utils.WarframeSocket;
import com.nyx.bot.repo.sys.SysUserRepository;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.IoUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class Runners {

    @Value("${test.isTest}")
    Boolean test;

    @Resource
    SysUserRepository userRepository;


    //程序启动完成后初始化Web系统用户
    @Bean
    public ApplicationRunner sysUser() {
        return args -> AsyncUtils.me().execute(() -> {
            SysUser user = new SysUser();
            user.setUserId(1L);
            user.setUserName("admin");
            // {bcrypt} 密码加密方式
            user.setPassword(new BCryptPasswordEncoder().encode("admin123"));
            List<SysUser> all = userRepository.findAll();
            if (all.isEmpty()) {
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
                WarframeDataSource.init();
                new WarframeSocket().connectServer(ApiUrl.WARFRAME_SOCKET);
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