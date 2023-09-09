package com.nyx.bot.aop;

import com.nyx.bot.entity.Services;
import com.nyx.bot.enums.ServicesEnums;
import com.nyx.bot.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Aspect
@Component
public class CodeCheckAspect {

    private static boolean isOpen() {
        ArrayList servers = CacheUtils.get(CacheUtils.SYSTEM, "service", ArrayList.class);
        for (Object server : servers) {
            if (server instanceof Services) {
                if (Objects.requireNonNull(((Services) server).getService()) == ServicesEnums.WARFRAME) {
                    return ((Services) server).getSwit();
                }
            }
        }
        return false;
    }

    /**
     * 是否开启Warframe查询服务
     */
    @Around(value = "execution(* com.nyx.bot.plugin.warframe.code.*.*(..))")
    public Object handler(ProceedingJoinPoint pjp) {
        try {
            if (isOpen()) {
                return pjp.proceed();
            }
            return 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
