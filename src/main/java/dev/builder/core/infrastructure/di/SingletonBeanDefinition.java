package dev.builder.core.infrastructure.di;

class SingletonBeanDefinition<T> extends BeanDefinition<T>{
    private final T instance;

    public SingletonBeanDefinition(Class<T> type, boolean isSingleton, T instance) {
        super(type, isSingleton);
        this.instance = instance;
    }

    public T getInstance() {
        return instance;
    }
}
