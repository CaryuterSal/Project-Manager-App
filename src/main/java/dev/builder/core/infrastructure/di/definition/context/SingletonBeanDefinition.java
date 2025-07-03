package dev.builder.core.infrastructure.di.definition.context;

import java.util.function.Supplier;

public class SingletonBeanDefinition<T> extends BeanDefinition<T> {
    private volatile T instance;
    private final InstantiationMode instantiationMode;

    public SingletonBeanDefinition(Class<T> type, String beanName, Supplier<? extends T> supplier, InstantiationMode instantiationMode) {
        super(type, beanName, supplier);
        this.instantiationMode = instantiationMode;
    }

    @Override
    public T getBean() {
        if(instance == null) {
            synchronized (this){
                if (this.instance == null){
                    this.instance = getSupplier().get();
                }
            }
        }
        return instance;
    }

    public InstantiationMode getInstantiationMode() {
        return instantiationMode;
    }
}
