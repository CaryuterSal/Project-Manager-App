package dev.builder.core.infrastructure.di.definition;

public interface FirstStep<T>{
    SingletonModeStep<T> singleton();
    OptionalConfigStep<T> prototype();
    OptionalConfigStep<T> asEagerSingleton();
    OptionalConfigStep<T> asLazySingleton();
}