package com.nyx.bot.aop;

import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.nyx.bot.modules.bot.service.BotsService;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class WhitelistBlacklistAspect {

    @Resource
    BotsService bs;

    // 白名单和黑名单过滤切面
    @Around(value = "execution(* com.nyx.bot.modules.*.plugin.*.*Handler(..))")
    public Object handler(ProceedingJoinPoint pjp) {
        try {
            for (Object arg : pjp.getArgs()) {
                switch (arg) {
                    case AnyMessageEvent any -> {
                        if (bs.isCheck(any.getGroupId(), any.getUserId())) {
                            return pjp.proceed();
                        }
                    }
                    case GroupMessageEvent group -> {
                        if (bs.isCheck(group.getGroupId(), group.getUserId())) {
                            return pjp.proceed();
                        }
                    }
                    case PrivateMessageEvent privateMessageEvent -> {
                        if (bs.isCheck(0L, privateMessageEvent.getUserId())) {
                            return pjp.proceed();
                        }
                    }
                    default -> {
                        return 0;
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return 0;
    }


}
