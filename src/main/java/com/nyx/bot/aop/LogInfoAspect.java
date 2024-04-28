package com.nyx.bot.aop;

import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.annotation.LogInfo;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.enums.BusinessStatus;
import com.nyx.bot.repo.sys.LogInfoRepository;
import com.nyx.bot.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LogInfoAspect {

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
            com.nyx.bot.entity.sys.LogInfo logInfo = new com.nyx.bot.entity.sys.LogInfo();
            logInfo.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            logInfo.setUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));

            if (e != null) {
                logInfo.setStatus(BusinessStatus.FAIL.ordinal());
                logInfo.setErrorMsg(e.getMessage());
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            logInfo.setRequestMethod(className + "." + methodName + "()");
            // 设置请求方式
            logInfo.setMethod(ServletUtils.getRequest().getMethod());
            logInfo.setRunTime(runTime);
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, logInfo, jsonResult);
            logInfo.setLogTime(new Date());
            // 保存数据库
            AsyncUtils.me().execute(() -> SpringUtils.getBean(LogInfoRepository.class).save(logInfo));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log     日志
     * @param logInfo 操作日志
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, LogInfo log, com.nyx.bot.entity.sys.LogInfo logInfo, Object jsonResult) {
        // 设置action动作
        logInfo.setBusinessType(log.businessType().getType());
        // 设置标题
        logInfo.setTitle(log.title());
        // 执行的命令
        logInfo.setCodes(log.codes());
        // 请求的群组
        logInfo.setGroupUid(logInfo.getGroupUid());
        // 请求的用户
        logInfo.setUserUid(logInfo.getUserUid());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, logInfo);
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult)) {
            logInfo.setResult(JSONObject.toJSONString(jsonResult));
        }
    }


    /**
     * 获取请求的参数，放到log中
     *
     * @param logInfo 操作日志
     */
    private void setRequestValue(JoinPoint joinPoint, com.nyx.bot.entity.sys.LogInfo logInfo) {
        Map<String, String[]> map = ServletUtils.getRequest().getParameterMap();
        if (StringUtils.isNotEmpty(map)) {
            String params = JSONObject.toJSONString(map);
            logInfo.setParam(params);
        } else {
            Object args = joinPoint.getArgs();
            if (StringUtils.isNotNull(args)) {
                Signature signature = joinPoint.getSignature();
                MethodSignature methodSignature = (MethodSignature) signature;
                String[] parameterNames = methodSignature.getParameterNames();
                Object[] value = joinPoint.getArgs();
                for (int i = 0; i < parameterNames.length; i++) {
                    if (StringUtils.isNotNull(parameterNames[i]) && StringUtils.isNotNull(value[i])) {
                        switch (parameterNames[i]) {
                            case "bot" -> logInfo.setBotUid(Long.valueOf(value[i].toString()));
                            case "group" -> logInfo.setGroupUid(Long.valueOf(value[i].toString()));
                            case "rawMsg" -> logInfo.setRawMsg(value[i].toString());
                            case "user" -> logInfo.setUserUid(Long.valueOf(value[i].toString()));
                            case "data" -> {
                                if (value[i] instanceof OneBotLogInfoData data) {
                                    logInfo.setBotUid(data.getBotUid());
                                    logInfo.setUserUid(data.getUserUid());
                                    logInfo.setRawMsg(data.getRawMsg());
                                    logInfo.setGroupUid(data.getGroupUid());
                                    logInfo.setPermissions(data.getPermissionsEnums().getStr());
                                }
                            }
                            default -> {
                            }
                        }
                    }
                }
                String params = argsArrayToString(value);
                logInfo.setParam(StringUtils.substring(params, 0, 2000));
            }
        }
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
                        Object jsonObj = JSONObject.toJSONString(o);
                        if (!MatcherUtils.isNumber(jsonObj.toString())) {
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
