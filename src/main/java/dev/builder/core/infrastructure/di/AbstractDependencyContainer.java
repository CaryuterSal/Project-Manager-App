package dev.builder.core.infrastructure.di;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class AbstractDependencyContainer implements DependencyContainer{

    /**
     * Mantiene las referencias a beans como clave valor, siendo la clave el nombre registrado para el bean
     */
    protected final ConcurrentMap<String, ? extends BeanDefinition<?>> registryByName = new ConcurrentHashMap<>();
MultiValuedMap<String, ? extends BeanDefinition<?>> d = new MultiValued
    /**
     * Mantiene las referencias a beans como clave valor, siendo la clave el tipo del bean
     */
    protected final ConcurrentMap<Class<?>, ? extends BeanDefinition<?>> registryByType = new ConcurrentHashMap<>();


    @Override
    public <T> @Nullable T getInstance(Class<T> clazz) {
        return null;
    }

    @Override
    public boolean isSingleton(Class<?> clazz) {
        return DependencyContainer.super.isSingleton(clazz);
    }

    @Override
    public <T> boolean register(Class<T> clazz, Provider<T> provider) {
        return false;
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
}
