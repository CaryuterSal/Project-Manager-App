package dev.builder.core.infrastructure.di;

class BeanDefinition<T> {
    private final Class<T> type;
    private final Provider<? extends T> provider;

    public BeanDefinition(Class<T> type, Provider<? extends T> provider) {
        this.type = type;
        this.provider = provider;
    }

    public Class<T> getType() {
        return type;
    }

    public Provider<? extends T> getProvider() {
        return provider;
    }
}
