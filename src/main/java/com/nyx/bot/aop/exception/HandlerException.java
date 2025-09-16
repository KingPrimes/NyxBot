package com.nyx.bot.aop.exception;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.exception.HtmlToImageException;
import com.nyx.bot.exception.ServiceException;
import com.nyx.bot.utils.I18nUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateInputException;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class HandlerException {

    @ResponseBody
    @ExceptionHandler(value = DataNotInfoException.class)
    public Object handlerDataNotInfoException(DataNotInfoException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body("<body>" + e.getMessage() + "</body>");
    }

    @ResponseBody
    @ExceptionHandler(value = HtmlToImageException.class)
    public Object handlerHtmlToImageException(HtmlToImageException e) {
        log.error("HtmlToImageException", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body(e.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(value = TemplateInputException.class)
    public Object handlerTemplateInputException(TemplateInputException html) {
        log.error("TemplateInputException", html);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body(html.getMessage());
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public void NoResourceFoundException(HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
    }

    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Object HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException", e);
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, I18nUtils.RequestErrorMethod());
    }

    @ResponseBody
    @ExceptionHandler(value = BadCredentialsException.class)
    public Object BadCredentialsException(BadCredentialsException e) {
        log.warn("BadCredentialsException", e);
        return AjaxResult.error(HttpCodeEnum.FAIL, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Object IllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException", e);
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ExpiredJwtException.class)
    public Object ExpiredJwtException(ExpiredJwtException e) {
        log.warn("ExpiredJwtException", e);
        return AjaxResult.error(HttpCodeEnum.INVALID_PARAM, HttpCodeEnum.INVALID_PARAM.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Object HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException", e);
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException:{} -{}", e.getBindingResult().getFieldError().getCode(),e.getBindingResult().getFieldError().getDefaultMessage());
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ServiceException.class)
    public Object ServiceException(ServiceException e) {
        return AjaxResult.error(HttpCodeEnum.ERROR, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object Exception(Exception e) {
        log.error("Exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("出现未知错误信息：" + e.getMessage());
    }

}
