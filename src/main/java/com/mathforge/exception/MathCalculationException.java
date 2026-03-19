package com.mathforge.exception;

public class MathCalculationException extends RuntimeException {
    public MathCalculationException(String message) {
        super(message);
    }

    public MathCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
