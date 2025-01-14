package com.nyx.bot.aop.exception;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.exception.HtmlToImageException;
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
    public Object handlerDataNotInfoException(DataNotInfoException dataNotInfoException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body("<body>" + dataNotInfoException.getMessage() + "</body>");
    }

    @ResponseBody
    @ExceptionHandler(value = HtmlToImageException.class)
    public Object handlerHtmlToImageException(HtmlToImageException html) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/html; charset=utf-8")
                .body(html.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(value = TemplateInputException.class)
    public Object handlerTemplateInputException(TemplateInputException html) {
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
    public Object HttpRequestMethodNotSupportedException() {
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, "请求方式错误");
    }

    @ResponseBody
    @ExceptionHandler(value = BadCredentialsException.class)
    public Object BadCredentialsException(BadCredentialsException e) {
        return AjaxResult.error(HttpCodeEnum.FAIL, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Object IllegalArgumentException(IllegalArgumentException e) {
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ExpiredJwtException.class)
    public Object ExpiredJwtException() {
        return AjaxResult.error(HttpCodeEnum.INVALID_PARAM, HttpCodeEnum.INVALID_PARAM.getMessage());
    }
    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Object HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return AjaxResult.error(HttpCodeEnum.INVALID_REQUEST, e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object Exception(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("出现未知错误信息：" + e.getMessage());
    }

}
