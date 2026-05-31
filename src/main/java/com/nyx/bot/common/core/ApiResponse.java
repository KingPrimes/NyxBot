package com.nyx.bot.common.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.utils.I18nUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ApiResponse<T> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 业务状态码
     */
    @JsonProperty
    private final int code;

    /**
     * 提示信息
     */
    @JsonProperty
    private final String msg;

    /**
     * 业务数据（可空）
     */
    @JsonProperty
    private final T data;

    private ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ===== 成功工厂 =====

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, I18nUtils.message("controller.success"), data);
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(200, I18nUtils.message("controller.success"), null);
    }

    public static ApiResponse<Void> ok(String msg) {
        return new ApiResponse<>(200, msg, null);
    }

    // ===== 错误工厂 =====

    public static <T> ApiResponse<T> error(int code, String msg, T data) {
        return new ApiResponse<>(code, msg, data);
    }

    public static ApiResponse<Void> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    // ===== 状态判断 =====

    public boolean isSuccess() {
        return code == 200;
    }

    public boolean isError() {
        return code != 200;
    }

    // ===== JSON 序列化（非控制器场景直接写响应） =====

    public String toJsonString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("ApiResponse 序列化失败: {}", e.getMessage(), e);
            return "{}";
        }
    }
}
