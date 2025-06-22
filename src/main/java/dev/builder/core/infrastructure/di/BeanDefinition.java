package dev.builder.core.infrastructure.di;

abstract class BeanDefinition<T> {
    private final Class<T> type;
    private final Provider<T> provider;

    public BeanDefinition(Class<T> type, Provider<T> provider) {
        this.type = type;
        this.provider = provider;
    }

    public Class<T> getType() {
        return type;
    }

    public Provider<T> getProvider() {
        return provider;
    }
}
