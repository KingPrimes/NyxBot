package com.nyx.bot.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

@SuppressWarnings("unused")
public class I18nUtils {
    private final MessageSource messageSource;

    public I18nUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String code, Object... args) {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    /**
     * 消息超时提示信息
     * Message TimeOut
     *
     * @return Message: TimeOut
     */
    public static String errorTimeOut() {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage("error.timeout", new Object[]{}, LocaleContextHolder.getLocale());
    }

    /**
     * 成功
     */
    public static String Success() {
        return message("result.success");
    }

    /**
     * 请求错误
     */
    public static String RequestError() {
        return message("request.error");
    }

    /**
     * 请求方法错误
     */
    public static String RequestErrorMethod() {
        return message("request.error.method");
    }

    /**
     * 请求参数错误
     */
    public static String RequestErrorParam() {
        return message("request.error.param");
    }

    public static String RequestValidServerUrl() {
        return message("request.valid.server.url");
    }

    /**
     * 请求客户端地址不符合规范
     */
    public static String RequestValidClientUrl() {
        return message("request.valid.client.url");
    }

    /**
     * 请求git地址不符合规范
     */
    public static String RequestValidGitUrl() {
        return message("request.valid.git.url");
    }

    /**
     * 超级管理员已存在且只能有一个
     */
    public static String PermissionsOne() {
        return message("permissions.one");
    }

    /**
     * 不可使用此权限
     */
    public static String PermissionsBan() {
        return message("permissions.ban");
    }

    public static String BWBlackExist() {
        return message("bw.black.exist");
    }

    public static String RequestTaskRun() {
        return message("request.task.run");
    }

    public static String ControllerRestPassWordOldError() {
        return message("controller.rest.password.old.error");
    }

    public static String ControllerRestPassWordONError() {
        return message("controller.rest.password.o.n.same");
    }

    public static String ControllerRestPassWordON() {
        return message("controller.rest.password.o.n");
    }

    public String getMessage(String msgKey, Object[] args) {
        return messageSource.getMessage(msgKey, args, LocaleContextHolder.getLocale());
    }

    public String getMessage(String msgKey) {
        return messageSource.getMessage(msgKey, new Object[]{}, LocaleContextHolder.getLocale());
    }
}
