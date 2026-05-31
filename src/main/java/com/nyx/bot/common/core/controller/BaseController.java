package com.nyx.bot.common.core.controller;


import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;

@Slf4j
public class BaseController {

    /**
     * 返回成功数据
     */
    public static ApiResponse<Object> success(Object data) {
        return ApiResponse.ok(data);
    }

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 响应请求分页数据
     */
    protected ApiResponse<PageData<?>> getDataTable(List<?> list, long total, long size, long current) {
        PageData<?> pageData = new PageData<>(total, size, current, list);
        return ApiResponse.ok(pageData);
    }

    protected ApiResponse<PageData<?>> getDataTable(List<?> list, long totalElements) {
        return getDataTable(list, totalElements, 0, 0);
    }

    protected ApiResponse<PageData<?>> getDataTable(Page<?> page) {
        PageData<?> pageData = new PageData<>(
                page.getTotalElements(),
                page.getSize(),
                page.getNumber() + 1,
                page.getContent()
        );
        return ApiResponse.ok(pageData);
    }

    /**
     * 获取request
     */
    public HttpServletRequest getRequest() {
        return ServletUtils.getRequest().orElse(null);
    }

    /**
     * 获取response
     */
    public HttpServletResponse getResponse() {
        return ServletUtils.getResponse().orElse(null);
    }

    /**
     * 获取session
     */
    public HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected ApiResponse<Void> toAjax(int rows) {
        return rows > 0 ? success() : error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected ApiResponse<Void> toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 返回成功
     */
    public ApiResponse<Void> success() {
        return ApiResponse.ok();
    }

    /**
     * 返回失败消息
     */
    public ApiResponse<Void> error() {
        return ApiResponse.error(500, I18nUtils.message("controller.error"));
    }

    /**
     * 返回成功消息
     */
    public ApiResponse<Void> success(String message) {
        return ApiResponse.ok(message);
    }

    public ApiResponse<Object> success(String msg, Object data) {
        return ApiResponse.ok(msg, data);
    }

    /**
     * 返回失败消息
     */
    public ApiResponse<Void> error(String message) {
        return ApiResponse.error(500, message);
    }

    /**
     * 返回错误码消息
     */
    public ApiResponse<Void> error(HttpStatus code, String message) {
        return ApiResponse.error(code.value(), message);
    }

}
