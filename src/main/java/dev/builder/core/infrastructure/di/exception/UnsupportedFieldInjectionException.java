package dev.builder.core.infrastructure.di.exception;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class UnsupportedFieldInjectionException extends RuntimeException {
    public UnsupportedFieldInjectionException(@NotNull Field field){
        super("Cannot inject field " + field.getName() + " into class " + field.getType().getName());
    }
    public UnsupportedFieldInjectionException(String message) {
        super(message);
    }
}
