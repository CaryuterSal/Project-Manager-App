package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.ConcurrentMultiValuedHashMap;
import dev.builder.core.infrastructure.di.definition.BeanDefinition;
import dev.builder.core.infrastructure.di.definition.BeanRegistrationConfiguration;
import dev.builder.core.infrastructure.di.definition.InstantiationMode;
import dev.builder.core.infrastructure.di.definition.SingletonBeanDefinition;
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
import java.util.stream.Stream;

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
    public <T> T getInstance(String beanName) {
        return getInstance(getBeanDefinition(beanName));
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
     * @return el bean
     * @param <T> el tipo del bean
     */
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

    /**
     * @throws NullPointerException si la configuración es nula
     * @throws ConstructorNotFoundException si la clase target no tiene al menos un constructor visible (público)
     */
    @Override
    public <T> boolean register(final BeanRegistrationConfiguration<T> config) {
        if(config == null) throw new NullPointerException("config is null");
        final Constructor<T> constructor = getMostSuitableConstructor(config.clazz());

        boolean registered = registerAllAssignableTypes(config, constructor);

        config.instatiationMode().ifPresentOrElse(mode -> {
            if(mode == InstantiationMode.EAGER) getInstance(config.clazz());
        }, () -> getInstance(config.clazz()));

        return registered;
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
     * Encuentra el constructor con más parámetros de cierta clase, lo que lo hace el más correcto para inyección de dependencias.
     * Este es completamente accesible
     * @param clazz la clase target
     * @return El mejor candidato de constructor, o {@code null} si ninguno existe
     * @param <T> el tipo de la clase target
     */
    @SuppressWarnings("unchecked")
    protected <T> Constructor<T> getMostSuitableConstructor(@NotNull Class<T> clazz){
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if(constructors.length == 0) {
            try {
                return clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new ConstructorNotFoundException(clazz.getName());
            }
        };

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
