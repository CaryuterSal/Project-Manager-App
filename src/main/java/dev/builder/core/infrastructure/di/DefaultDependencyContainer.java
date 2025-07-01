package dev.builder.core.infrastructure.di;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class DefaultDependencyContainer extends AbstractDependencyContainer {

    private static DefaultDependencyContainer INSTANCE;

    private DefaultDependencyContainer() {}
    public static DefaultDependencyContainer getInstance(){
        if(INSTANCE == null){
            INSTANCE = new DefaultDependencyContainer();
        }
        return INSTANCE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getInstance(Class<T> clazz) {
        BeanDefinition<T> targetDependency = (BeanDefinition<T>) registryByType.get(clazz);
        if(targetDependency.isEmpty()) throw new DependencyNotFoundException(clazz);
        Class<?>[] argsTypes = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
        try {
            Constructor<?> constructor = clazz.getConstructor(argsTypes);
            return (T) constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isSingleton(Class<?> clazz) {
        Annotation singletonAnnotation = clazz.getAnnotation(Singleton.class);
        Annotation beanAnnotation
    }

    @Override
    public <T> boolean register(Class<T> clazz, Provider<T> provider) {
        return false;
    }

    @Override
    public <T> boolean register(Class<T> clazz, String beanName, Provider<T> provider) {
        return false;
    }

    @Override
    public <T> boolean registerSingleton(Class<T> clazz, T instance) {
        return false;
    }

    @Override
    public <T> boolean registerSingleton(Class<T> clazz, String beanName, T instance) {
        return false;
    }

    @Override
    public <T> boolean registerSingleton(Class<T> clazz, Provider<T> provider) {
        return false;
    }

    @Override
    public <T> boolean registerSingleton(Class<T> clazz, String beanName, Provider<T> provider) {
        return false;
    }

    @Override
    public <T> boolean register(Class<T> clazz, Provider<T> provider, BeanScope scope) {
        return false;
    }

    @Override
    public <T> boolean register(Class<T> clazz, String beanName, Provider<T> provider, BeanScope scope) {
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

    @Contract(pure = true)
    private static boolean parametersMatch(Class<?>[] expectedTypes, Object @NotNull [] expectedMatchingValues){
        for(int i = 0; i < expectedMatchingValues.length; i++){
            if(!expectedMatchingValues[i].equals(expectedTypes[i])){
                return false;
            }
        }
        return true;
    }

}
