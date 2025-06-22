package dev.builder.core.infrastructure.di;

import org.jetbrains.annotations.NotNull;

public class DependencyNotFoundException extends RuntimeException {
    public DependencyNotFoundException(String message) {
        super(message);
    }
    public DependencyNotFoundException(@NotNull Class<?> clazz) {
      super("No registered dependency was found for class %s".formatted(clazz.getSimpleName()));
    }
}
