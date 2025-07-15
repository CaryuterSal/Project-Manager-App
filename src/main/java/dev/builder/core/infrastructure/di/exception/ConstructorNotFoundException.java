package dev.builder.core.infrastructure.di.exception;

public class ConstructorNotFoundException extends RuntimeException {
    public ConstructorNotFoundException(Class<?> clazz) {
        super("Constructor not found for class " + clazz.getName());
    }
    public ConstructorNotFoundException(String message) {
        super(message);
    }
}
