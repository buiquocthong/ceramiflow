package com.ceramiflow.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String m) {
        super(m);
    }
}