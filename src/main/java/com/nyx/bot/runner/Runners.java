package com.nyx.bot.runner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.bot.entity.BotAdmin;
import com.nyx.bot.modules.bot.repo.BotAdminRepository;
import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.utils.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            String password = StringUtils.getRandomString();
            // {bcrypt} 密码加密方式
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            log.info("\u001B[31m默认账号：admin 随机密码：{} \t 请修改随机密码，或保存好随机密码！ \u001B[0m", password);
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

    @Bean
    public ApplicationRunner updateOver() {
        return args -> {
            File folder = new File("./backup");
            File[] listOfFiles = folder.listFiles();
            boolean fileExists = false;
            if (listOfFiles != null) {
                log.debug("./backup 不为空");
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().contains(DateUtils.getDate(new Date(), DateUtils.NOT_HMS))) {
                        fileExists = true;
                        break;
                    }
                }
            }
            if (fileExists) {
                log.debug("./backup/bake_{} 存在", DateUtils.getDate(new Date(), DateUtils.NOT_HMS));
                Map<Long, Bot> robots = SpringUtils.getBean(BotContainer.class).robots;
                if (robots != null) {
                    Optional<BotAdmin> admin = SpringUtils.getBean(BotAdminRepository.class).findByPermissions(PermissionsEnums.SUPER_ADMIN);
                    for (Bot bot : robots.values()) {
                        admin.ifPresent(a -> {
                            if (a.getBotUid().equals(bot.getSelfId())) {
                                bot.sendPrivateMsg(a.getAdminUid(), "更新完成!", false);
                            }
                        });
                    }
                }
            }


        };
    }
}