package com.nyx.bot.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.annotation.LogInfo;
import com.nyx.bot.enums.BusinessStatus;
import com.nyx.bot.modules.system.repo.LogInfoRepository;
import com.nyx.bot.utils.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LogInfoAspect {

    @Resource
    ObjectMapper objectMapper;

    Long runTime;

    //切入点
    @Pointcut("@annotation(com.nyx.bot.annotation.LogInfo)")
    public void logPointCut() {

    }

    @Around(value = "@annotation(logInfo)")
    public Object doAround(ProceedingJoinPoint pjp, LogInfo logInfo) {
        Object obj;
        try {
            long starTime = System.currentTimeMillis();
            obj = pjp.proceed();
            runTime = System.currentTimeMillis() - starTime;
            handleLog(pjp, logInfo, null, "");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    @AfterThrowing(value = "@annotation(logInfo)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, LogInfo logInfo, Exception e) {
        handleLog(joinPoint, logInfo, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, LogInfo controllerLog, final Exception e, Object jsonResult) {
        try {

            // *========数据库日志=========*//
            com.nyx.bot.modules.system.entity.LogInfo logInfo = new com.nyx.bot.modules.system.entity.LogInfo();
            logInfo.setStatus(BusinessStatus.SUCCESS.ordinal());
            ServletUtils.getRequest().ifPresentOrElse(r -> logInfo.setUrl(StringUtils.substring(r.getRequestURI(), 0, 255)), () -> logInfo.setUrl(""));

            if (e != null) {
                logInfo.setStatus(BusinessStatus.FAIL.ordinal());
                logInfo.setErrorMsg(e.getMessage());
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            logInfo.setRequestMethod(className + "." + methodName + "()");

            ServletUtils.getRequest().ifPresentOrElse(r -> logInfo.setMethod(r.getMethod()), () -> logInfo.setMethod("Bot"));


            logInfo.setRunTime(runTime);
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, logInfo, jsonResult);
            logInfo.setLogTime(new Date());
            // 保存数据库
            AsyncUtils.me().execute(() -> SpringUtils.getBean(LogInfoRepository.class).save(logInfo));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage(), exp);
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log     日志
     * @param logInfo 操作日志
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, LogInfo log, com.nyx.bot.modules.system.entity.LogInfo logInfo, Object jsonResult) {
        // 设置action动作
        logInfo.setBusinessType(log.businessType().getType());
        logInfo.setTitle(log.title());
        logInfo.setCode(log.code());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, logInfo);
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult)) {
            try {
                logInfo.setResult(objectMapper.writeValueAsString(jsonResult));
            } catch (Exception e) {
                logInfo.setResult("{}");
            }
        }
    }


    /**
     * 获取请求的参数，放到log中
     *
     * @param logInfo 操作日志
     */
    private void setRequestValue(JoinPoint joinPoint, com.nyx.bot.modules.system.entity.LogInfo logInfo) {
        ServletUtils.getRequest().ifPresentOrElse(r -> {
            Map<String, String[]> map = r.getParameterMap();
            if (StringUtils.isNotEmpty(map)) {
                try {
                    String params = objectMapper.writeValueAsString(map);
                    logInfo.setParam(params);
                } catch (Exception e) {
                    logInfo.setParam("{}");
                }
            }
        }, () -> {
            Object args = joinPoint.getArgs();
            if (StringUtils.isNotNull(args)) {
                Object[] value = joinPoint.getArgs();
                String params = argsArrayToString(value);
                logInfo.setParam(StringUtils.substring(params, 0, 2000));
            }
        });
    }


    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o)) {
                    try {
                        String jsonObj = objectMapper.writeValueAsString(o);
                        if (!MatcherUtils.isNumber(jsonObj)) {
                            params
                                    .append(jsonObj)
                                    .append(" ");
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return params.toString().trim();
    }


}
