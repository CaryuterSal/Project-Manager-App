package dev.builder.core.infrastructure.di;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class AnnotationAwareDependencyContainer extends AbstractDependencyContainer{

    private static AnnotationAwareDependencyContainer INSTANCE;

    private AnnotationAwareDependencyContainer() {}
    public static AnnotationAwareDependencyContainer getInstance(){
        if(INSTANCE == null){
            INSTANCE = new AnnotationAwareDependencyContainer();
        }
        return INSTANCE;
    }

    private final ConcurrentMap<Class<?>, Object> registry = new ConcurrentHashMap<>();

    @Override
    public <T> @Nullable T getInstance(Class<T> clazz) {
        return null;
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
