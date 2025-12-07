package com.nyx.bot.common.core.controller;


import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.page.TableDataInfo;
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
    public static AjaxResult success(Object data) {
        return AjaxResult.success(I18nUtils.message("result.success"), data);
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
        data.setRecords(list);
        data.setTotal(totalElements);
        rspData.setData(data);
        return rspData;
    }


    protected TableDataInfo getDataTable(Page<?> page) {
        TableDataInfo td = new TableDataInfo();
        TableDataInfo.Data data = new TableDataInfo.Data();
        data.setTotal(page.getTotalElements());
        data.setSize(page.getSize());
        data.setRecords(page.getContent());
        data.setCurrent(page.getNumber() + 1);
        td.setData(data);
        td.setMsg(I18nUtils.message("result.success"));
        td.setCode(HttpStatus.OK.value());
        return td;
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
        return new AjaxResult(HttpStatus.OK, msg, data);
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
    public AjaxResult error(HttpStatus code, String message) {
        return new AjaxResult(code, message);
    }

}
