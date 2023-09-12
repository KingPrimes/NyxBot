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

    private static boolean isOpen(ServicesEnums enums) {
        ArrayList servers = CacheUtils.get(CacheUtils.SYSTEM, "service", ArrayList.class);
        for (Object server : servers) {
            if (server instanceof Services) {
                if (Objects.requireNonNull(((Services) server).getService()) == enums) {
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
    public Object handlerWarframe(ProceedingJoinPoint pjp) {
        try {
            if (isOpen(ServicesEnums.WARFRAME)) {
                return pjp.proceed();
            }
            return 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //Acg图片
    @Around(value = "execution(* com.nyx.bot.plugin.acg.code.*.*(..))")
    public Object handlerAcg(ProceedingJoinPoint pjp) {
        try {
            if (isOpen(ServicesEnums.ACG_IMAGE)) {
                return pjp.proceed();
            }
            return 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //ChatGpt
    @Around(value = "execution(* com.nyx.bot.plugin.chat.code.*.*(..))")
    public Object handlerChat(ProceedingJoinPoint pjp) {
        try {
            if (isOpen(ServicesEnums.CHAT_GPT)) {
                return pjp.proceed();
            }
            return 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //表情包
    @Around(value = "execution(* com.nyx.bot.plugin.expression.code.ExpressionCode.expression(..))")
    public Object handlerExpression(ProceedingJoinPoint pjp) {
        try {
            if (isOpen(ServicesEnums.DRAW_EMOJIS)) {
                return pjp.proceed();
            }
            return 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //点歌
    @Around(value = "execution(* com.nyx.bot.plugin.music.code.*.*(..))")
    public Object handlerMusic(ProceedingJoinPoint pjp) {
        try {
            if (isOpen(ServicesEnums.MUSIC)) {
                return pjp.proceed();
            }
            return 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //鉴别图片
    @Around(value = "execution(* com.nyx.bot.plugin.nsfw.code.*.*(..))")
    public Object handlerNsfw(ProceedingJoinPoint pjp) {
        try {
            if (isOpen(ServicesEnums.LOOK_IMAGE)) {
                return pjp.proceed();
            }
            return 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


}
