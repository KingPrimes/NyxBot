package com.nyx.bot.aop;

import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.nyx.bot.repo.impl.black.BlackService;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Aspect
@Component
public class BlackCheckAspect {


    //黑名单过滤
    @Around(value = "execution(* com.nyx.bot.plugin.*.*(..))")
    public Object handler(ProceedingJoinPoint pjp) {
        try {
            for (Object arg : pjp.getArgs()) {
                if (arg instanceof AnyMessageEvent) {
                    AtomicReference<Long> groupId = new AtomicReference<>(0L);
                    Optional.ofNullable(((AnyMessageEvent) arg).getGroupId()).ifPresent(groupId::set);
                    if (SpringUtils.getBean(BlackService.class).isBlack(groupId.get(), ((AnyMessageEvent) arg).getUserId())) {
                        return pjp.proceed();
                    }
                }
                if (arg instanceof GroupMessageEvent) {
                    if (SpringUtils.getBean(BlackService.class).isBlack(((GroupMessageEvent) arg).getGroupId(), ((GroupMessageEvent) arg).getUserId())) {
                        return pjp.proceed();
                    }
                }
                if (arg instanceof PrivateMessageEvent) {
                    if (SpringUtils.getBean(BlackService.class).isBlack(0L, ((PrivateMessageEvent) arg).getUserId())) {
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
