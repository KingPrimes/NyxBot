package com.nyx.bot.aop;

import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.nyx.bot.repo.impl.black.BlackService;
import com.nyx.bot.utils.SpringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BlackCheckAspect {

    //黑名单过滤
    @Around("execution(* com.nyx.bot.plugin.*.*Handler(..))")
    public Object handler(ProceedingJoinPoint pjp) {
        Object obj;
        try {
            obj = pjp.proceed();
            for (Object arg : pjp.getArgs()) {
                if (arg instanceof AnyMessageEvent) {
                    if (SpringUtils.getBean(BlackService.class).isBlack(((AnyMessageEvent) arg).getGroupId(), ((AnyMessageEvent) arg).getUserId())) {
                        return obj;
                    }
                }
                if (arg instanceof GroupMessageEvent) {
                    if (SpringUtils.getBean(BlackService.class).isBlack(((GroupMessageEvent) arg).getGroupId(), ((GroupMessageEvent) arg).getUserId())) {
                        return obj;
                    }
                }
                if (arg instanceof PrivateMessageEvent) {
                    if (SpringUtils.getBean(BlackService.class).isBlack(0L, ((PrivateMessageEvent) arg).getUserId())) {
                        return obj;
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

}
