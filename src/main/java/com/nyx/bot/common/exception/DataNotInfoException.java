package com.nyx.bot.common.exception;

public class DataNotInfoException extends Exception {
    public DataNotInfoException() {
    }

    public DataNotInfoException(String message) {
        super(message);
    }

    public DataNotInfoException(String message, Throwable t) {
        super(message, t);
    }
}
