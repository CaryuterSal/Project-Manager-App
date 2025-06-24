package dev.builder.core.infrastructure.di;

import dev.builder.core.ConcurrentMultiValuedHashMap;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class AbstractDependencyContainer implements DependencyContainer{

    /**
     * Mantiene las referencias a beans como clave valor, siendo la clave el nombre registrado para el bean
     */
    protected final ConcurrentMap<String, ? extends BeanDefinition<?>> registryByName = new ConcurrentHashMap<>();
    /**
     * Mantiene las referencias a beans como clave valor, siendo la clave el tipo del bean
     */
    protected final ConcurrentMultiValuedHashMap<Class<?>, BeanDefinition<?>> registryByType = new ConcurrentMultiValuedHashMap<>();

    @Override
    public <T> @Nullable T getInstance(Class<T> clazz) {
        return getInstance(getBeanDefinition(clazz));
    }

    @Override
    public <T> @Nullable T getInstance(String beanName) {
        return getInstance(getBeanDefinition(beanName));
    }

    protected <T> T getInstance(BeanDefinition<T> beanDefinition) {
        if(beanDefinition instanceof SingletonBeanDefinition<T>){
            return  ((SingletonBeanDefinition<T>) beanDefinition).getInstance();
        } else {
            return beanDefinition.getProvider().get(this);
        }
    }


    @SuppressWarnings("unchecked")
    protected <T> BeanDefinition<T> getBeanDefinition(Class<T> clazz){
        Set<? extends BeanDefinition<?>> relatedBeans = registryByType.get(clazz);
        if(relatedBeans.isEmpty()) throw new BeanNotFoundException(clazz);
        if(relatedBeans.size() != 1) throw new UncertainBeanRetrievalException(clazz);
        return (BeanDefinition<T>) relatedBeans.iterator().next();
    }


    @SuppressWarnings("unchecked")
    protected <T> BeanDefinition<T> getBeanDefinition(String beanName){
        BeanDefinition<T> bean = (BeanDefinition<T>) registryByName.get(beanName);
        if(bean == null) throw new BeanNotFoundException(beanName);
        return bean;
    }

    @Override
    public boolean isSingleton(Class<?> clazz) {
        return getBeanDefinition(clazz) instanceof SingletonBeanDefinition;
    }

    @Override
    public boolean isSingleton(String beanName) {
        return getBeanDefinition(beanName) instanceof SingletonBeanDefinition;
    }

    @Override
    public <T> boolean register(Class<T> clazz, Provider<? extends T> provider) {
        BeanDefinition<T> beanDefinition = new BeanDefinition<>(clazz, provider);
        if(registryByType.containsMapping(clazz, beanDefinition)){
            return false;
        }
        String beanName = generateBeanName(clazz);

    }

    @Override
    public <T> boolean registerSingleton(Class<T> clazz, T instance) {
        return false;
    }

    @Override
    public <T> boolean registerSingleton(Class<T> clazz, Provider<T> provider) {
        return false;
    }

    @Override
    public <T> boolean register(Class<T> clazz, Provider<T> provider, BeanScope scope) {
        return false;
    }

    @Override
    public void scanPackage(String packageName) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean isRegistered(Class<?> type) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Set<Class<?>> getRegisteredTypes() {
        return Set.of();
    }

    private @NotNull String generateBeanName(@NotNull Class<?> clazz) {
        String className = clazz.getName();
        char firstLetter = className.charAt(0);
        return Character.toLowerCase(firstLetter) + className.substring(1);
    }
}
