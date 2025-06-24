package dev.builder.core.infrastructure.di;

import org.jetbrains.annotations.NotNull;

public class BeanNotFoundException extends BeanContainerException {
    public BeanNotFoundException(String beanName) {
        super(beanName);
    }
    public BeanNotFoundException(@NotNull Class<?> clazz) {
        super(clazz);
    }
}
