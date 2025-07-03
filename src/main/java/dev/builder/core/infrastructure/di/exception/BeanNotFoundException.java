package dev.builder.core.infrastructure.di.exception;

import org.jetbrains.annotations.NotNull;

public class BeanNotFoundException extends BeanContainerException {
    public BeanNotFoundException(String beanName) {
        super(beanName);
    }
    public BeanNotFoundException(@NotNull Class<?> clazz) {
        super(clazz);
    }
}
