package dev.builder.core.infrastructure.di.definition.context;

public interface BuildStep<T>{
    BeanRegistrationConfiguration<T> build();
}
