package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.ConcurrentMultiValuedHashMap;
import dev.builder.core.infrastructure.di.annotation.Eager;
import dev.builder.core.infrastructure.di.constructor.ConstructorResolver;
import dev.builder.core.infrastructure.di.constructor.FaillingConstructorResolver;
import dev.builder.core.infrastructure.di.constructor.NoParamConstructorChooser;
import dev.builder.core.infrastructure.di.constructor.ParameterCountConstructorResolver;
import dev.builder.core.infrastructure.di.definition.*;
import dev.builder.core.infrastructure.di.exception.BeanNotFoundException;
import dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException;
import dev.builder.core.infrastructure.di.exception.UncertainBeanRetrievalException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

abstract class AbstractDependencyContainer implements DependencyContainer {

    /**
     * Mantiene las referencias a beans como clave valor, siendo la clave el nombre registrado para el bean
     */
    protected final ConcurrentMap<String, BeanDefinition<?>> registryByName = new ConcurrentHashMap<>();
    /**
     * Mantiene las referencias a beans como clave valor, siendo la clave el tipo del bean
     */
    protected final ConcurrentMultiValuedHashMap<Class<?>, BeanDefinition<?>> registryByType = new ConcurrentMultiValuedHashMap<>();

    /**
     * @throws BeanNotFoundException si no se encuentra el bean
     * @throws UncertainBeanRetrievalException si hay más de un bean registrado para dicha clase
     */
    @Override
    public <T> T getInstance(Class<T> clazz) {
        return getInstance(getBeanDefinition(clazz));
    }

    /**
     * @throws BeanNotFoundException si no se encuentra el bean
     */
    @Override
    public <T> T getInstance(Class<T> clazz, String beanName) {
        return getInstance(getBeanDefinition(clazz, beanName));
    }

    /**
     * @throws BeanNotFoundException si no se encuentra el bean
     */
    protected <T> T getInstance(@NotNull BeanDefinition<T> beanDefinition) {
        return beanDefinition.getBean();
    }

    /**
     * @throws BeanNotFoundException si no se encuentra el bean
     * @throws UncertainBeanRetrievalException si hay más de un bean registrado para dicha clase
     */
    @SuppressWarnings("unchecked")
    protected <T> BeanDefinition<T> getBeanDefinition(Class<T> clazz){
        Set<? extends BeanDefinition<?>> relatedBeans = registryByType.get(clazz);
        if(relatedBeans.isEmpty()) throw new BeanNotFoundException(clazz);
        if(relatedBeans.size() != 1) throw new UncertainBeanRetrievalException(clazz);
        return (BeanDefinition<T>) relatedBeans.iterator().next();
    }


    /**
     * Busca en el registro del contenedor un bean con un determinado nombre
     * @param beanName el nombre del bean
     * @throws BeanNotFoundException si no hay bean para dicho nombre
     * @throws ClassCastException si el bean con dicho nombre no es del tipo que se especifica
     * @return el bean
     * @param <T> el tipo del bean
     */
    protected <T> BeanDefinition<T> getBeanDefinition(@NotNull Class<T> clazz, String beanName){
        BeanDefinition<?> rawBean =  getBeanDefinition(beanName);
        if(!clazz.isAssignableFrom(rawBean.getType())){
            throw new ClassCastException("Bean '" + beanName + "' is not of type " + clazz.getName());
        }

        @SuppressWarnings("unchecked")
        BeanDefinition<T> typedBean = (BeanDefinition<T>) rawBean;
        return typedBean;
    }

    /**
     * Busca en el registro del contenedor un bean con un determinado nombre.
     * </br>
     * NO ES TYPE SAFE
     * @param beanName el nombre del bean
     * @throws BeanNotFoundException si no hay bean para dicho nombre
     * @return el bean
     */
    protected BeanDefinition<?> getBeanDefinition(String beanName){
        BeanDefinition<?> rawBean =  registryByName.get(beanName);
        if(rawBean == null) throw new BeanNotFoundException(beanName);
        return rawBean;
    }

    @Override
    public boolean isSingleton(Class<?> clazz) {
        return isSingleton(getBeanDefinition(clazz));
    }

    @Override
    public boolean isSingleton(String beanName) {
        return isSingleton(getBeanDefinition(beanName));
    }

    protected boolean isSingleton(BeanDefinition<?> beanDefinition) {
        return beanDefinition instanceof SingletonBeanDefinition;
    }

    @Override
    public InstantiationMode getInstantiationMode(Class<?> clazz) {
        BeanDefinition<?> bean = getBeanDefinition(clazz);
        if(!isSingleton(bean)) throw new BeanNotFoundException("Bean with class '" + clazz + "' is not singleton, hence, does not have instantiation mode");

        SingletonBeanDefinition<?> singletonBeanDefinition = (SingletonBeanDefinition<?>) bean;
        return singletonBeanDefinition.getInstantiationMode();
    }

    @Override
    public InstantiationMode getInstantiationMode(String beanName) {
        BeanDefinition<?> bean = getBeanDefinition(beanName);
        if(!isSingleton(bean)) throw new BeanNotFoundException("Bean '" + beanName + "' is not singleton, hence, does not have instantiation mode");

        SingletonBeanDefinition<?> singletonBeanDefinition = (SingletonBeanDefinition<?>) bean;
        return singletonBeanDefinition.getInstantiationMode();
    }

    /**
     * @throws NullPointerException si la configuración es nula
     * @throws ConstructorNotFoundException si la clase target no tiene al menos un constructor visible (público)
     */
    @Override
    public <T> boolean register(final BeanRegistrationConfiguration<T> config) {
        if(config == null) throw new NullPointerException("config is null");
        @SuppressWarnings("unchecked")
        Constructor<T> constructor = ((ConstructorResolver<T>)getConstructorResolver()).resolve(config.clazz());
        boolean registered = registerAllAssignableTypes(config, constructor);

        config.instatiationMode().ifPresent(mode -> {
            if(mode == InstantiationMode.EAGER) getInstance(config.clazz());
        });

        return registered;
    }

    protected <T> ConstructorResolver<T> getConstructorResolver() {
        return new FaillingConstructorResolver<>(
                new ParameterCountConstructorResolver<>()
        );
    }

    @Override
    public <T> boolean register(Class<T> clazz) {
        return register(createDefaultRegistrationConfiguration(clazz));
    }

    /**
     * Crea la configuración de creación por defecto para un nuevo bean.
     * @param clazz la clase del bean a registrar
     * @return la configuración de registro del bean
     * @param <T> el tipo del bean
     */
    protected <T> BeanRegistrationConfiguration<T> createDefaultRegistrationConfiguration(Class<T> clazz){
        return BeanRegistrationConfiguration.builder(clazz)
                .asLazySingleton().build();
    }

    /**
     * Registra un bean para la configuración y constructor especificados para todos los tipos a los que es asignable, recursivamente
     * <ul>
     *     <li>Interfaces</li>
     *     <li>Clases padre</li>
     * </ul>
     * @param config la configuración de creación
     * @param constructor el constructor del bean
     * @return si se agregó al menos un registro para el bean
     * @param <T> el tipo del bean
     */
    protected <T> boolean registerAllAssignableTypes(BeanRegistrationConfiguration<T> config, Constructor<T> constructor) {
        BeanDefinition<T> beanDefinition = toBeanDefinition(config, constructor);
        boolean registered = false;
        for(Class<?> assignableType: getAllAssignableTypes(config.clazz())) {
            if(registerBeanDefinition(assignableType, beanDefinition)){
                registered = true;
            };
        }
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
    protected <T> @NotNull BeanDefinition<T> toBeanDefinition(@NotNull BeanRegistrationConfiguration<T> config, Constructor<T> constructor) {

        String beanName = config.beanName().orElse(generateBeanName(config.clazz()));

        Supplier<T> beanCreator = () -> {
            try {
                List<? extends BeanDefinition<?>> paramBeans = getBeanDefinitionsForConstructorParams(constructor);
                Object[] paramInstances = paramBeans.stream().map(BeanDefinition::getBean).toArray();
                T instance = constructor.newInstance((Object[]) paramInstances);
                if(config.initCustomizer().isPresent()) config.initCustomizer().get().accept(instance);
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
        String className = clazz.getSimpleName();
        char firstLetter = className.charAt(0);
        return Character.toLowerCase(firstLetter) + className.substring(1);
    }

    /**
     * Registra un bean definition en el registro por nombre y tipos de manera síncrona (Thread-safe).
     * En caso de que el bean ya exista no se hace ningún cambio
     * @param beanDefinition el contenedor con la información del bean
     * @return verdadero si el bean es nuevo, false si ya existía
     * @param <T> El tipo del bean
     */
    protected synchronized <T> boolean registerBeanDefinition(Class<?> implType, @NotNull BeanDefinition<T> beanDefinition){
        if(registryByType.containsMapping(implType, beanDefinition)){
            return false;
        }
        registryByName.putIfAbsent(beanDefinition.getBeanName(), beanDefinition);
        registryByType.put(implType, beanDefinition);
        return true;
    }

    /**
     * Recorre todas las clases e interfaces que implementa un tipo.
     * De utilidad a la hora de registrar los beans, ya que esto permite
     * obtener un bean por medio de sus superclases o interfaces
     * @param clazz la clase del bean
     * @return una lista con el bean, las interfaces que implementa y superclases
     */
    protected @NotNull Set<Class<?>> getAllAssignableTypes(Class<?> clazz) {
        Set<Class<?>> types = new HashSet<>();
        Queue<Class<?>> queue = new ArrayDeque<>();
        queue.add(clazz);
        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            types.add(current);
            Class<?> superclass = current.getSuperclass();
            if (superclass != null && superclass != Object.class && types.add(superclass)) {
                queue.add(superclass);
            }
            for (Class<?> iface : current.getInterfaces()) {
                if (types.add(iface)) {
                    queue.add(iface);
                }
            }
        }
        return types;
    }

    @Override
    public void initialize() {
        for(Map.Entry<String, BeanDefinition<?>> entry : registryByName.entrySet()) {
            getInstance(entry.getValue().getType(), entry.getKey());
        }
    }

    @Override
    public boolean isRegistered(Class<?> type) {
        return registryByType.containsKey(type);
    }

    @Override
    public boolean isRegistered(String beanName) {
        return registryByName.containsKey(beanName);
    }

    @Override
    public synchronized void clear() {
        registryByType.clear();
        registryByName.clear();
    }

    @Override
    public Set<Class<?>> getRegisteredTypes() {
        return Collections.unmodifiableSet(registryByType.keySet());
    }

    @Override
    public Set<String> getRegisteredBeanNames() {
        return Collections.unmodifiableSet(registryByName.keySet());
    }
}
