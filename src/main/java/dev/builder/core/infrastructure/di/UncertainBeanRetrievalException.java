package dev.builder.core.infrastructure.di;

import org.jetbrains.annotations.NotNull;

public class UncertainBeanRetrievalException extends BeanContainerException {

    public UncertainBeanRetrievalException(String beanName) {
        super(beanName);
    }

    public UncertainBeanRetrievalException(@NotNull Class<?> clazz) {
        super(clazz);
    }
}
