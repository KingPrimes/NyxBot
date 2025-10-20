package com.nyx.bot.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum HttpCodeEnum {
    SUCCESS(200, "成功"),
    NOT_MODIFIED(304, "未修改"),
    FAIL(400, "失败"),
    ERROR(500, "错误"),
    WARN(301, "警告"),
    NOT_FOUND(404, "资源不存在"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    REQUEST_TIMEOUT(408, "请求超时"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),
    INVALID_REQUEST(400, "无效请求"),
    INVALID_TOKEN(401, "无效token"),
    INVALID_PARAM(400, "无效参数"),
    INVALID_USER(400, "无效用户"),
    INVALID_PASSWORD(400, "无效密码"),
    INVALID_CODE(400, "无效验证码"),
    INVALID_ID(400, "无效id"),
    /**
     * 429 Too Many Requests
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    /**
     * 509 Bandwidth Limit Exceeded
     */
    BANDWIDTH_LIMIT_EXCEEDED(509, "请求带宽超出限制"),
    ;

    private final int code;
    private final String message;

    HttpCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static HttpCodeEnum getCode(int code) {
        List<HttpCodeEnum> collect = Arrays.stream(HttpCodeEnum.values()).filter(v -> v.code == code).toList();
        if (collect.isEmpty()) {
            return HttpCodeEnum.ERROR;
        }
        return collect.getFirst();
    }

}
