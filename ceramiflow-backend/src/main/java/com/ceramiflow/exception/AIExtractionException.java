package com.ceramiflow.exception;

public class AIExtractionException extends RuntimeException {
    public AIExtractionException(String m) {
        super(m);
    }

    public AIExtractionException(String m, Throwable t) {
        super(m, t);
    }
}