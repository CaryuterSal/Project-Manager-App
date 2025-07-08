package dev.builder.core.infrastructure.di.definition;

public interface BuildStep<T>{
    BeanRegistrationConfiguration<T> build();
}
