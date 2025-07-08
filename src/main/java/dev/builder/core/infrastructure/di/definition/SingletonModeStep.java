package dev.builder.core.infrastructure.di.definition;

public interface SingletonModeStep<T> {
    OptionalConfigStep<T> eager();

    OptionalConfigStep<T> lazy();
}
