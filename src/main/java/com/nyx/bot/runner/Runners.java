package com.nyx.bot.runner;

import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.SysUser;
import com.nyx.bot.repo.SysUserRepository;
import com.nyx.bot.utils.IoUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class Runners {

    @Value("${test.isTest}")
    Boolean test;

    @Resource
    SysUserRepository userRepository;


    //程序启动完成启动浏览器
    @Bean
    public ApplicationRunner browser() {

        return args -> {
            if (!test) {
                IoUtils.index();
            }
        };
    }

    //程序启动完成后初始化Web系统用户
    @Bean
    public ApplicationRunner sysUser() {
        return new ApplicationRunner() {
            @Async("myAsync")
            @Override
            public void run(ApplicationArguments args) {
                SysUser user = new SysUser();
                user.setUserId(1L);
                user.setUserName("admin");
                user.setPassword("admin123");
                List<SysUser> all = userRepository.findAll();
                if (all.isEmpty()){
                    SysUser save = userRepository.save(user);
                    log.info("用户不存在！已添加新用户：{}",save);
                }
            }
        };
    }


    //程序启动完成后初始化数据
    @Bean
    public ApplicationRunner inItDataSource() {
        return new ApplicationRunner() {
            @Async("myAsync")
            @Override
            public void run(ApplicationArguments args) {
                new WarframeDataSource().init();
            }
        };
    }




}
