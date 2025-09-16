package com.nyx.bot.aop.exception;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.onebot.CqMatcher;
import com.nyx.bot.utils.onebot.CqParse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class PluginHandlerException {

    // 定义切入点：匹配被@Shiro注解的类中带有@AnyMessageHandler注解的方法
    @Pointcut("@annotation(anyMessageHandler) && within(@com.mikuac.shiro.annotation.common.Shiro *)")
    public void pluginMethodPointcut(AnyMessageHandler anyMessageHandler) {
    }

    // 环绕通知，捕获异常并记录日志
    @SuppressWarnings("unused")
    @Around(value = "pluginMethodPointcut(anyMessageHandler)", argNames = "joinPoint,anyMessageHandler")
    public Object aroundPluginMethod(ProceedingJoinPoint joinPoint, AnyMessageHandler anyMessageHandler) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MessageHandlerFilter filterAnnotation = method.getAnnotation(MessageHandlerFilter.class);
        String cmd = filterAnnotation != null ? StringUtils.removeMatcher(filterAnnotation.cmd()) : "unknown_cmd";
        Object[] args = joinPoint.getArgs();
        Bot bot = null;
        AnyMessageEvent event = null;
        for (Object arg : args) {
            if (arg instanceof Bot b) {
                bot = b;
            }
            if (arg instanceof AnyMessageEvent e) {
                event = e;
            }
        }
        try {
            log.debug("群：{} 用户:{} 使用了 {} 指令 指令参数：{}", Objects.requireNonNull(event).getGroupId(), event.getUserId(), cmd, event.getRawMessage());
            if (CqMatcher.isCqAt(Objects.requireNonNull(event).getRawMessage())) {
                CqParse build = CqParse.build(event.getRawMessage());
                Bot finalBot = bot;
                if (build.getCqAt().stream().anyMatch(a -> a.equals(Objects.requireNonNull(finalBot).getSelfId()))) {
                    event.setRawMessage(build.reovmCq().trim());
                    event.setMessage(build.reovmCq().trim());
                }
            }
            // 执行目标方法
            return joinPoint.proceed(new Object[]{bot, event});
        } catch (Exception ex) {

            log.debug("捕获到@AnyMessageHandler注解方法的异常。方法: {}, 参数: {}, 异常信息: {}",
                    joinPoint.getSignature().toShortString(), Arrays.toString(args), ex.getMessage(), ex);

            if (bot != null && event != null) {
                ArrayMsgUtils ams = ArrayMsgUtils.builder();
                ams.text("插件" + cmd + "执行异常 \n 异常信息：");
                ams.text(ex.getMessage());
                bot.sendMsg(event, ams.build(), false);
            }
            return 0;
        }
    }
}
