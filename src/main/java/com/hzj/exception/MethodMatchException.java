package com.hzj.exception;

public class MethodMatchException extends RuntimeException {

    public MethodMatchException(String message) {
        super(message);
    }

    public MethodMatchException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public  MethodMatchException(Throwable throwable) {
        super(throwable);
    }
}
