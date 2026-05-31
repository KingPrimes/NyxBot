package com.nyx.bot.aop.exception;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.ServiceException;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateInputException;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class HandlerException {

    @ResponseBody
    @ExceptionHandler(value = DataNotInfoException.class)
    public Object handlerDataNotInfoException(DataNotInfoException e) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header("Content-Type", "text/html; charset=utf-8")
                .body("<body>" + e.getMessage() + "</body>");
    }

    @ResponseBody
    @ExceptionHandler(value = TemplateInputException.class)
    public Object handlerTemplateInputException(TemplateInputException html) {
        log.error("TemplateInputException", html);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
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
        return ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), I18nUtils.RequestErrorMethod());
    }

    @ResponseBody
    @ExceptionHandler(value = BadCredentialsException.class)
    public Object BadCredentialsException(BadCredentialsException e) {
        return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Object IllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException", e);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ExpiredJwtException.class)
    public Object ExpiredJwtException(ExpiredJwtException e) {
        log.warn("ExpiredJwtException", e);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), I18nUtils.RequestErrorParam());
    }

    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Object HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException", e);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Object MissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "缺少必填参数: " + e.getParameterName());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String messageKey = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        assert messageKey != null;
        String i18nMessage = I18nUtils.message(messageKey);
        String finalMessage = i18nMessage.equals(messageKey) ? messageKey : i18nMessage;
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), finalMessage);
    }

    @ResponseBody
    @ExceptionHandler(value = ServiceException.class)
    public Object ServiceException(ServiceException e) {
        log.error("ServiceException", e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    @ExceptionHandler(value = AsyncRequestNotUsableException.class)
    public void AsyncRequestNotUsableException() {
        // SSE / 异步请求客户端断开，正常生命周期，无需处理
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object Exception(Exception e) {
        log.error("Exception", e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "出现未知错误信息：" + e.getMessage());
    }

}
