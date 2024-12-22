package com.nyx.bot.aop.exception;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.exception.HtmlToImageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateInputException;

@Slf4j
@RestControllerAdvice
public class HandlerException {

    @ResponseBody
    @ExceptionHandler(value = DataNotInfoException.class)
    public Object handlerDataNotInfoException(DataNotInfoException dataNotInfoException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body("<body>" + dataNotInfoException.getMessage() + "</body>");
    }

    @ResponseBody
    @ExceptionHandler(value = HtmlToImageException.class)
    public Object handlerHtmlToImageException(HtmlToImageException html) {
        log.info("HtmlToImageException");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body(html.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(value = TemplateInputException.class)
    public Object handlerTemplateInputException(TemplateInputException html) {
        log.info("TemplateInputException");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body(html.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = NoResourceFoundException.class)
    public Object NoResourceFoundException() {
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, HttpCodeEnum.INVALID_REQUEST.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class, HttpMessageNotReadableException.class})
    public Object HttpRequestMethodNotSupportedException() {
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, "请求方式错误");
    }

    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Object HttpMessageNotReadableException() {
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, "请求地址错误");
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object Exception(Exception e) {
        log.error("出现未知错误信息：{} --- 错误类：{}", e.getMessage(), e.getClass());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("出现未知错误信息：" + e.getMessage());
    }

}
