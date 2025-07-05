package dev.builder.core.infrastructure.di.definition;

public interface BeanNameStep<T> extends BuildStep<T> {
    BuildStep<T> withName(String name);
}