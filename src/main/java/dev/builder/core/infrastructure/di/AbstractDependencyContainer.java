package dev.builder.core.infrastructure.di;

import dev.builder.core.ConcurrentMultiValuedHashMap;
import dev.builder.core.infrastructure.di.definition.context.*;
import dev.builder.core.infrastructure.di.exception.BeanNotFoundException;
import dev.builder.core.infrastructure.di.exception.UncertainBeanRetrievalException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractDependencyContainer implements DependencyContainer{

    /**
     * Mantiene las referencias a beans como clave valor, siendo la clave el nombre registrado para el bean
     */
    protected final ConcurrentMap<String, BeanDefinition<?>> registryByName = new ConcurrentHashMap<>();
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
            return  ((SingletonBeanDefinition<T>) beanDefinition).getBean();
        } else {
            return beanDefinition.getSupplier().get();
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
    public <T> boolean register(final BeanRegistrationConfiguration<T> config) {
        if(config == null) throw new NullPointerException("config is null");
        final Constructor<T> constructor = getMostSuitableConstructor(config.clazz());
        if(constructor == null) throw new IllegalArgumentException("No suitable constructor found for " + config.clazz());

        BeanDefinition<T> beanDefinition = toBeanDefinition(config, constructor);

        boolean registered = registerBeanDefinition(beanDefinition);
        config.instatiationMode().ifPresentOrElse(mode -> {
            if(mode == InstantiationMode.EAGER) getInstance(config.clazz());
        }, () -> getInstance(config.clazz()));

        return registered;
    }

    /**
     * Traduce desde una solicitud de registro de bean (aka {@link BeanRegistrationConfiguration}) a una definición interna del bean
     * </br>
     * @param config la solicitud de registro
     * @param constructor el constructor del bean
     * @return la definición de bean SIN REGISTRAR
     * @param <T> el tipo del bean
     */
    private <T> @NotNull BeanDefinition<T> toBeanDefinition(@NotNull BeanRegistrationConfiguration<T> config, Constructor<T> constructor) {

        String beanName = config.beanName().orElse(generateBeanName(config.clazz()));

        Supplier<T> beanCreator = () -> {
            try {
                T instance = constructor.newInstance(getBeanDefinitionsForConstructorParams(constructor));
                if(config.initCustomizer().isPresent()) config.initCustomizer().get().apply(instance);
                return instance;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        return switch (config.beanScope()){
            case PROTOTYPE -> new BeanDefinition<>(config.clazz(), beanName, beanCreator);
            case SINGLETON ->  new SingletonBeanDefinition<>(config.clazz(), beanName, beanCreator, config.instatiationMode().get());
        };
    }

    /**
     * Método de utilidad para encontrar la lista de definiciones del bean para todos los parámetros del constructor
     * @param constructor el constructor target, no debe ser nulo
     * @return la lista de definiciones de bean
     */
    private List<? extends BeanDefinition<?>> getBeanDefinitionsForConstructorParams(@NotNull Constructor<?> constructor){
        return Arrays.stream(constructor.getParameterTypes())
                .map(this::getBeanDefinition)
                .toList();
    }

    /**
     * Genera el nombre del bean dependiendo de la clase. La estrategia única es usar {@code cammelCase} con el nombre corto de clase
     * @param clazz la clase del bean
     * @return el nombre generado del bean
     */
    private @NotNull String generateBeanName(@NotNull Class<?> clazz) {
        String className = clazz.getName();
        char firstLetter = className.charAt(0);
        return Character.toLowerCase(firstLetter) + className.substring(1);
    }

    /**
     * Encuentra el constructor con más parámetros de cierta clase, lo que lo hace el más correcto para inyección de dependencias.
     * Este es completamente accesible
     * @param clazz la clase target
     * @return El mejor candidato de constructor, o {@code null} si ninguno existe
     * @param <T> el tipo de la clase target
     */
    @SuppressWarnings("unchecked")
    protected <T> Constructor<T> getMostSuitableConstructor(@NotNull Class<T> clazz){
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if(constructors.length == 0) return null;
        Constructor<?> constructor = Stream.of(constructors)
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .get();
        constructor.setAccessible(true);
        return (Constructor<T>) constructor;
    }

    /**
     * Registra un bean definition en el registro por nombre y tipos de manera síncrona (Thread-safe).
     * En caso de que el bean ya exista no se hace ningún cambio
     * @param beanDefinition el contenedor con la información del bean
     * @return verdadero si el bean es nuevo, false si ya existía
     * @param <T> El tipo del bean
     */
    private synchronized <T> boolean registerBeanDefinition(@NotNull BeanDefinition<T> beanDefinition){
        if(registryByName.containsKey(beanDefinition.getBeanName())){
            return false;
        }
        registryByName.putIfAbsent(beanDefinition.getBeanName(), beanDefinition);
        registryByType.put(beanDefinition.getType(), beanDefinition);
        return true;
    }

    @Override
    public void scanPackage(String packageName) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("not yet");
    }

    @Override
    public void initialize() {
        for(BeanDefinition<?> beanDefinition : registryByName.values()){
            if(beanDefinition instanceof SingletonBeanDefinition<?>){
                if(((SingletonBeanDefinition<?>) beanDefinition).getInstantiationMode().equals(InstantiationMode.LAZY)){
                    beanDefinition.getBean();
                }
            }
        }
    }

    @Override
    public boolean isRegistered(Class<?> type) {
        return registryByType.containsKey(type);
    }

    @Override
    public synchronized void clear() {
        registryByType.clear();
        registryByName.clear();
    }

    @Override
    public Set<Class<?>> getRegisteredTypes() {
        return registryByType.keySet();
    }

    @Override
    public Set<String> getRegisteredBeanNames() {
        return registryByName.keySet();
    }
}
