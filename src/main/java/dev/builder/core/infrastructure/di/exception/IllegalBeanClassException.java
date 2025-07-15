package dev.builder.core.infrastructure.di.exception;

public class IllegalBeanClassException extends RuntimeException {
    public IllegalBeanClassException(String message) {
        super(message);
    }
}
