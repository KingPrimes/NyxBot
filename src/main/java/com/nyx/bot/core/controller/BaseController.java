package com.nyx.bot.core.controller;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.ServletUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 返回成功数据
     */
    public static AjaxResult success(Object data) {
        return AjaxResult.success("操作成功", data);
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
    protected TableDataInfo getDataTable(List<?> list, Long totalElements) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        TableDataInfo.Data data = new TableDataInfo.Data();
        data.setContent(list);
        data.setTotalElements(totalElements);
        rspData.setData(data);
        return rspData;
    }

    @SuppressWarnings({"rawtypes"})
    protected ResponseEntity getDataTable(Page page) {
        return ResponseEntity.ok(AjaxResult.success(page));
    }

    /**
     * 获取request
     */
    public HttpServletRequest getRequest() {
        return ServletUtils.getRequest();
    }

    /**
     * 获取response
     */
    public HttpServletResponse getResponse() {
        return ServletUtils.getResponse();
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
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? success() : error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 返回成功
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    public AjaxResult success(String msg, Object data) {
        return new AjaxResult(HttpCodeEnum.SUCCESS, msg, data);
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * 返回错误码消息
     */
    public AjaxResult error(HttpCodeEnum code, String message) {
        return new AjaxResult(code, message);
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }

    public String pushJson(List<?> all) {
        SimplePropertyPreFilter spf = new SimplePropertyPreFilter();
        Set<String> set = new HashSet<>();
        set.add("pageNum");
        set.add("pageSize");
        set.add("totalCount");
        set.add("totalPage");
        spf.getExcludes().addAll(set);
        return JSON.toJSONString(all, spf);
    }

}
