package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.runtime.BeanRetriever;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;

@Bean
public class DependantOnContainer {

    private final BeanRetriever beanRetriever;

    public DependantOnContainer(BeanRetriever beanRetriever) {
        this.beanRetriever = beanRetriever;
    }

    public BeanRetriever beanRetriever() {
        return beanRetriever;
    }
}
