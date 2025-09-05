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
public class BlackCheckAspect {

    @Resource
    BotsService bs;

    //黑白名单过滤
    @Around(value = "execution(* com.nyx.bot.modules.*.plugin.*.*Handler(..))")
    public Object handler(ProceedingJoinPoint pjp) {
        try {
            for (Object arg : pjp.getArgs()) {
                if (arg instanceof AnyMessageEvent) {
                    if (bs.isCheck(((AnyMessageEvent) arg).getGroupId(), ((AnyMessageEvent) arg).getUserId())) {
                        return pjp.proceed();
                    }
                }
                if (arg instanceof GroupMessageEvent) {
                    if (bs.isCheck(((GroupMessageEvent) arg).getGroupId(), ((GroupMessageEvent) arg).getUserId())) {
                        return pjp.proceed();
                    }
                }
                if (arg instanceof PrivateMessageEvent) {
                    if (bs.isCheck(0L, ((PrivateMessageEvent) arg).getUserId())) {
                        return pjp.proceed();
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return 0;
    }


}
