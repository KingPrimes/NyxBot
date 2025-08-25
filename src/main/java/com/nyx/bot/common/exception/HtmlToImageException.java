package com.nyx.bot.common.exception;

public class HtmlToImageException extends Exception {
    public HtmlToImageException() {
    }

    public HtmlToImageException(String message) {
        super(message);
    }

    public HtmlToImageException(String message, Throwable t) {
        super(message, t);
    }
}
