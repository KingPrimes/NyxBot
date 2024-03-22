package com.nyx.bot.aop.exception;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.exception.HtmlToImageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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
    @ExceptionHandler(value = Exception.class)
    public Object Exception(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("出现未知错误信息：" + e.getMessage());
    }

}
