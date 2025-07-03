package dev.builder.core.infrastructure.di.definition.context;

import java.util.function.Supplier;

public class BeanDefinition<T> {
    private final Class<T> type;
    private final String beanName;
    private final Supplier<? extends T> supplier;

    public BeanDefinition(Class<T> type, String beanName, Supplier<? extends T> supplier) {
        if(type == null || supplier == null || beanName == null) throw new NullPointerException("bean name or type or supplier is null");
        this.type = type;
        this.supplier = supplier;
        this.beanName = beanName;
    }

    final public Class<T> getType() {
        return type;
    }

    public Supplier<? extends T> getSupplier() {
        return supplier;
    }

    public T getBean(){
        return supplier.get();
    }

    public final String getBeanName(){
        return beanName;
    }
}
