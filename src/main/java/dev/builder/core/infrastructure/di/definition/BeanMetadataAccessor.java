package dev.builder.core.infrastructure.di.definition;

public interface BeanMetadataAccessor {
    /**
     * @param clazz el tipo de bean
     * @return true si el tipo es {@link BeanScope#SINGLETON}
     */
    boolean isSingleton(Class<?> clazz);

    /**
     * @param beanName el nombre del bean
     * @return true si el tipo es {@link BeanScope#SINGLETON}
     */
    boolean isSingleton(String beanName);

    /**
     * @param clazz el tipo de bean
     * @return si es Lazy o Eager por defecto
     */
    InstantiationMode getInstantiationMode(Class<?> clazz);

    /**
     * @param beanName el nombre del bean
     * @return si es Lazy o Eager por defecto
     */
    InstantiationMode getInstantiationMode(String beanName);
}
