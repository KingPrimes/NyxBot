package com.nyx.bot.runner;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.Services;
import com.nyx.bot.entity.sys.SysMenu;
import com.nyx.bot.entity.sys.SysUser;
import com.nyx.bot.entity.sys.SysUserAndMenu;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.enums.ServicesEnums;
import com.nyx.bot.plugin.warframe.utils.WarframeSocket;
import com.nyx.bot.repo.ServicesRepository;
import com.nyx.bot.repo.sys.SysMenuRepository;
import com.nyx.bot.repo.sys.SysUserAndMenuRepository;
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
import java.util.Objects;

@Slf4j
@Component
public class Runners {

    @Value("${test.isTest}")
    Boolean test;

    @Resource
    SysUserRepository userRepository;

    @Resource
    ServicesRepository repository;

    @Resource
    SysMenuRepository menu;

    @Resource
    SysUserAndMenuRepository um;

    @Bean
    public ApplicationRunner initMenu() {
        return args -> {
            List<SysMenu> ms = menu.findAll();
            List<SysUserAndMenu> ums = um.findAll();
            if (ms.isEmpty()) {
                List<SysMenu> array = Objects.requireNonNull(JSON.parseArray(Runners.class.getResourceAsStream("/menu/menu"))).toJavaList(SysMenu.class);
                menu.saveAll(array);
            }
            if (ums.isEmpty()) {
                List<SysUserAndMenu> array = Objects.requireNonNull(JSON.parseArray(Runners.class.getResourceAsStream("/menu/user_and_menu"))).toJavaList(SysUserAndMenu.class);
                um.saveAll(array);
            }
        };
    }

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
                SysUser save = userRepository.save(user);
                log.info("用户不存在！已添加新用户：{}", save);
            }
        }, AsyncBeanName.SERVICE);
    }


    //程序启动完成后初始化数据
    /*@Bean
    public ApplicationRunner inItDataSource() {
        return args -> {
            if (!test) {
                AsyncUtils.me().execute(WarframeDataSource::init);
            }
        };
    }*/

  /*  @Bean
    public ApplicationRunner warframeDataSocket() {
        return args -> {
            if (!test) {
                //创建WebSocket链接 或许游戏数据
                WarframeSocket.socket().connectServer(ApiUrl.WARFRAME_SOCKET);
            }
        };
    }*/

    /**
     * 初始化服务
     */
    @Bean
    public ApplicationRunner service() {
        List<Services> all = repository.findAll();
        if (ServicesEnums.values().length != all.size()) {
            return args -> {
                for (ServicesEnums enums : ServicesEnums.values()) {
                    Services service = new Services();
                    service.setService(enums);
                    service.setSwit(false);
                    repository.save(service);
                }
            };
        } else {
            return args -> {
            };
        }
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