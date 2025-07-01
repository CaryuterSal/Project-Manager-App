package dev.builder.core.infrastructure.di;

public interface Provider<T> {
    T get(DependencyContainer dc);
}
