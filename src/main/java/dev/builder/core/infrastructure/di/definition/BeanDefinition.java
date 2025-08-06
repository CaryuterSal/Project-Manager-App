package dev.builder.core.infrastructure.di.definition;

import java.util.function.Supplier;

public class BeanDefinition<T> implements Cloneable {
    private final Class<T> type;
    private final String beanName;
    private final Supplier<? extends T> supplier;

    public BeanDefinition(Class<T> type, String beanName, Supplier<? extends T> supplier) {
        if(type == null || supplier == null || beanName == null) throw new NullPointerException("bean name or type or supplier is null");
        this.type = type;
        this.supplier = supplier;
        this.beanName = beanName;
    }
    
    private BeanDefinition(BeanDefinition<T> beanDefinition) {
        if(beanDefinition == null) throw new NullPointerException("beanDefinition is null");
        this.type = beanDefinition.type;
        this.beanName = beanDefinition.beanName;
        this.supplier = beanDefinition.supplier;
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

    @Override
    protected BeanDefinition<T> clone() throws CloneNotSupportedException {
        return new BeanDefinition<>(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BeanDefinition<?> that = (BeanDefinition<?>) o;
        return getType().equals(that.getType()) && getBeanName().equals(that.getBeanName());
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getBeanName().hashCode();
        return result;
    }
}
