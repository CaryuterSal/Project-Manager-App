package dev.builder.core.infrastructure.di.definition.context;

public interface SingletonModeStep<T> {
    BeanNameStep<T> eager();

    BeanNameStep<T> lazy();
}
