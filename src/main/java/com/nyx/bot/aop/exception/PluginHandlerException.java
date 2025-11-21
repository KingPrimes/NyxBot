package com.nyx.bot.aop.exception;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.BusinessStatus;
import com.nyx.bot.enums.BusinessType;
import com.nyx.bot.enums.LogTitleEnum;
import com.nyx.bot.modules.bot.service.BotsService;
import com.nyx.bot.modules.system.entity.LogInfo;
import com.nyx.bot.modules.system.repo.LogInfoRepository;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.onebot.CqMatcher;
import com.nyx.bot.utils.onebot.CqParse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class PluginHandlerException {

    @Resource
    BotsService bs;

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
        // 黑白名单检查是否继续执行后续操作
        if (event != null && !bs.isCheck(event.getGroupId(), event.getUserId())) {
            return 0;
        }
        long startTime = System.currentTimeMillis();
        Exception ex = null;
        Object result = null;
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
            startTime = System.currentTimeMillis();
            // 执行目标方法
            return joinPoint.proceed(new Object[]{bot, event});
        } catch (Exception e) {
            ex = e;
            log.debug("捕获到@AnyMessageHandler注解方法的异常。方法: {}, 参数: {}, 异常信息: {}",
                    joinPoint.getSignature().toShortString(), Arrays.toString(args), ex.getMessage(), e);

            if (bot != null && event != null) {
                ArrayMsgUtils ams = ArrayMsgUtils.builder();
                ams.text("插件" + cmd + "执行异常 \n 异常信息：");
                ams.text(ex.getMessage());
                bot.sendMsg(event, ams.build(), false);
            }
            return 0;
        } finally {
            Bot finalBot1 = bot;
            AnyMessageEvent finalEvent = event;
            long finalStartTime = startTime;
            Exception finalEx = ex;
            // 异步保存日志
            AsyncUtils.me().execute(() -> recordPluginLog(joinPoint, cmd, finalBot1, finalEvent, finalStartTime, finalEx));
        }
    }

    /**
     * 记录插件操作日志
     */
    private void recordPluginLog(ProceedingJoinPoint joinPoint, String cmd, Bot bot, AnyMessageEvent event, long startTime, Exception ex) {
        try {
            // 设置方法信息
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            LogInfo logInfo = LogInfo.builder()
                    .title(LogTitleEnum.PLUGIN)
                    .status(ex != null ? BusinessStatus.FAIL.ordinal() : BusinessStatus.SUCCESS.ordinal())
                    .errorMsg(ex != null ? ex.getMessage() : null)
                    .url("")
                    .method("Plugin")
                    .businessType(BusinessType.PLUGIN.getType())
                    .requestMethod(className + "." + methodName + "()")
                    .runTime(System.currentTimeMillis() - startTime)
                    .code(cmd)
                    .userUid(event != null ? event.getUserId() : null)
                    .groupUid(event != null ? event.getGroupId() : null)
                    .rawMsg(event != null ? event.getRawMessage() : null)
                    .botUid(bot != null ? bot.getSelfId() : null)
                    .logTime(new Date())
                    .build();

            // 空安全检查：确保 logInfo 不为 null 后再保存
            if (logInfo != null) {
                SpringUtils.getBean(LogInfoRepository.class).save(logInfo);
            }
        } catch (Exception exp) {
            log.error("插件日志记录异常", exp);
        }
    }
}
