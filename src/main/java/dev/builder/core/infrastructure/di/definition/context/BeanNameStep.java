package dev.builder.core.infrastructure.di.definition.context;

public interface BeanNameStep<T> extends BuildStep<T> {
    BuildStep<T> withName(String name);
}