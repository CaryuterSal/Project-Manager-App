package dev.builder.core.infrastructure.di.definition;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SingletonBeanDefinition<?> that = (SingletonBeanDefinition<?>) o;
        return Objects.equals(instance, that.instance) && getInstantiationMode() == that.getInstantiationMode();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(instance);
        result = 31 * result + getInstantiationMode().hashCode();
        return result;
    }
}
