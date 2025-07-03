package dev.builder.core.infrastructure.di.definition.context;
public enum BeanScope {
    SINGLETON(SingletonBeanDefinition.class),
    PROTOTYPE(BeanDefinition.class);

    private final Class<?> beanDefinitionClass;
    BeanScope(Class<?> beanClass) {
        this.beanDefinitionClass = beanClass;
    }

    public Class<?> beanDefinitionClass() {
        return beanDefinitionClass;
    }
}
