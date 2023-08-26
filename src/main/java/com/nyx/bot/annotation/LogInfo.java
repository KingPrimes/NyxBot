package com.nyx.bot.annotation;

import com.nyx.bot.enums.BusinessType;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.PermissionsEnums;

import java.lang.annotation.*;

/**
 * 自定义日志记录注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogInfo {

    /**
     * 模块
     */
    String title() default "其它";

    /**
     * 执行的命令
     */
    Codes codes() default Codes.TYPE_CODE;

    /**
     * 操作
     */
    BusinessType businessType() default BusinessType.OTHER;

    //用户等级
    PermissionsEnums permissions() default PermissionsEnums.OTHER;

    /**
     * 请求得群组
     */
    long group() default 0;

    /**
     * 请求得人员
     */
    long user() default 0;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;

}
