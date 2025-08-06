package dev.builder.core.infrastructure.di.exception;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class ConstructorConflictException extends RuntimeException {
    public <T> ConstructorConflictException(@NotNull Class<T> clazz, @NotNull List<Constructor<T>> constructors ) {
        super("Multiple candidate constructors were found in " + clazz.getSimpleName() + " with " + String.join(" : ", constructors.stream().map(Constructor::toString).toList()));
    }

    public <T> ConstructorConflictException(@NotNull Class<T> clazz, Constructor<T>... constructors ) {
        super("Multiple candidate constructors were found in " + clazz.getSimpleName() + " with " + String.join(" : ", Arrays.stream(constructors).map(Constructor::toString).toList()));
    }
    public ConstructorConflictException(String message) {
        super(message);
    }
}
