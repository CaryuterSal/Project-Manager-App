package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.Lazy;
import dev.builder.core.infrastructure.di.annotation.Singleton;
import dev.builder.core.infrastructure.di.definition.*;
import dev.builder.core.infrastructure.di.exception.BeanNotFoundException;
import dev.builder.core.infrastructure.di.exception.UnsupportedFieldInjectionException;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationAwareDependencyContainer extends AbstractDependencyContainer{

    private static volatile AnnotationAwareDependencyContainer INSTANCE;

    private AnnotationAwareDependencyContainer() {}

    /**
     * Getter de la clase usando el patrón <a href=https://refactoring.guru/es/design-patterns/singleton>Singleton</a>.
     * </br>
     * Es <b>Thread-safe</b>
     * @return La instancia de la clase
     */
    public static AnnotationAwareDependencyContainer getInstance(){
        if(INSTANCE == null){
            synchronized (AnnotationAwareDependencyContainer.class){
                if(INSTANCE == null){
                    INSTANCE = new AnnotationAwareDependencyContainer();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * @throws BeanNotFoundException si algún bean, ya sea por inyección de campo o constructor no puede inyectarse
     * @throws dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException si no se encuentra un constructor para instance el bean
     */
    @Override
    public void scanPackage(String packageName) {
        PackageScanner packageScanner = new PackageScanner(packageName);
        Set<Class<?>> packageClasses = packageScanner.scan();
        packageClasses.stream()
                .filter(AnnotationAwareDependencyContainer::isAnnotatedBean)
                .forEach(this::register);
    }
    /**
     * Crea la configuración por defecto de creación tomando en cuenta las anotaciones presentes:
     * <ul>
     *     <li>Usa {@link Bean}, {@link Singleton} y {@link Lazy} para determinar el tipo de bean, scope y modo de instancia</li>
     *     <li>Registra un nombre de bean custom usando las anotaciones anteriores (si está presente)</li>
     *     <li>Registra la inyección por campos usando el {@link InitCustomizer}</li>
     * </ul>
     * @param clazz la clase del bean a registrar
     * @return la configuración de registro
     * @param <T> el tipo de bean
     */
    @Override
    protected <T> BeanRegistrationConfiguration<T> createDefaultRegistrationConfiguration(Class<T> clazz) {

        OptionalConfigStep<T> configStep = isAnnotatedSingleton(clazz)
                ? (isAnnotatedLazy(clazz)
                ? BeanRegistrationConfiguration.builder(clazz).asLazySingleton()
                : BeanRegistrationConfiguration.builder(clazz).asEagerSingleton())
                : BeanRegistrationConfiguration.builder(clazz).prototype();

        BeanRegistrationConfiguration<T> config;
        Optional<String> beanName = extractAnnotatedBeanName(clazz);
        if (beanName.isPresent()) {
            config =  configStep.withName(beanName.get()).build();
        } else {
            config = configStep.build();
        }
        return addFieldInjectConfig(config);

    }

    /**
     * Añade la configuración para la inyección de dependencias por medio de campos de clase.
     * </br>
     * Para esto lo añade como un paso del {@link InitCustomizer} que se ejecuta antes de todos (si es que existen), sino, se agrega uno nuevo
     * @param sourceConfig la configuración base
     * @return una nueva configuración si hay campos inyectables, o la misma si no los hay
     * @param <T> el tipo de bean a registrarse
     * @throws BeanNotFoundException si alguno de los campos no tiene un bean resoluble en el contenedor de dependencias
     */
    private <T> @NotNull BeanRegistrationConfiguration<T> addFieldInjectConfig(@NotNull BeanRegistrationConfiguration<T> sourceConfig){
        Set<Field> injectableFields = getAnnotatedFields(sourceConfig.clazz());
        if(injectableFields.isEmpty()) return sourceConfig;

        InitCustomizer<T> injectFieldsCustomizer = bean -> {
            for (Field field : injectableFields) injectField(bean, field);
        };
        return sourceConfig.prependInitCustomizer(injectFieldsCustomizer);
    }

    /**
     * Dado un campo, le inyecta la dependencia del contenedor, si existe
     * @param bean el bean del campo
     * @param field el campo a inyectar
     * @throws BeanNotFoundException si no existe un bean para inyectar
     */
    private void injectField(Object bean, @NotNull Field field){
        field.setAccessible(true);
        try {
            field.set(bean, getInstance(field.getType()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Escoge el mejor constructor candidato para la inyección de dependencias para la determinada clase.
     * La estrategia que se sigue es la siguiente:
     * </br>
     * <ol>
     * <li>Si no hay ningún constructor se devuelve nulo </li>
     * <li>Si hay constructores anotados con {@link Inject} se escoge el que tenga más parámetros</li>
     * <li>Si no, si hay constructores con parámetros anotados con {@link Inject} se escoge el que tenga más parámetros</li>
     * <li>Si no, se escoge el constructor con más parámetros, o el constructor vacío</li>
     * </ol>
     * @param clazz la clase target
     * @return el constructor más viable según la estrategia, o {@code null} si la clase no tiene constructor (Ej. es una interface o anotación)
     * @param <T> el tipo de la clase.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Constructor<T> getMostSuitableConstructor(@NotNull Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) return null;

        List<Constructor<?>> orderedConstructors = getOrderedPublicConstructors(constructors);
        List<Constructor<?>> annotatedConstructors = getInjectAnnotatedConstructors(orderedConstructors);

        if (annotatedConstructors.size() == 1) {
            return (Constructor<T>) annotatedConstructors.getFirst();
        }

        return (Constructor<T>) orderedConstructors.stream()
                .filter(c -> !annotatedConstructors.contains(c))
                .filter(c -> getAnnotatedParamCount(c) == 0)
                .max(Comparator.comparingInt(this::getAnnotatedParamCount))
                .orElseGet(() -> super.getMostSuitableConstructor(clazz));
    }


    /**
     * Ordena la lista de constructores de mayor a menor en base a su número de parámetros,
     * filtrándolos, dejando solo aquellos que son públicos.
     * @param constructors el arreglo de constructores
     * @return lista ordenada de constructores
     */
    private List<Constructor<?>> getOrderedPublicConstructors(Constructor<?>[] constructors) {
        Comparator<Constructor<?>> comparator =  Comparator.comparing(Constructor::getParameterCount);
        comparator = comparator.reversed();
        return Arrays.stream(constructors)
                .filter(c -> Modifier.isPublic(c.getModifiers()))
                .sorted(comparator)
                .toList();
    }

    /**
     * Filtra una lista de constructores para aquellos que tengan la anotación {@link Inject}
     * @param constructors la lista de constructores
     * @return lista filtrada de constructores
     */
    private List<Constructor<?>> getInjectAnnotatedConstructors(@NotNull List<Constructor<?>> constructors) {
        return constructors.stream()
                .filter(AnnotationAwareDependencyContainer::isAnnotatedInjectConstructor)
                .toList();
    }

    /**
     * Obtiene el número de parámetros del constructor
     * @param constructor el constructor target. No puede ser {@code null}
     * @return la cantidad de parámetros, {@code 0} si no tiene ninguno
     */
    protected int getAnnotatedParamCount(@NotNull Constructor<?> constructor){
        return getInjectableParams(constructor).size();
    }

    protected List<Parameter> getInjectableParams(@NotNull Constructor<?> constructor){
        Parameter[] params = constructor.getParameters();
        return Arrays.stream(params)
                .filter(AnnotationAwareDependencyContainer::isAnnotatedInjectParam)
                .toList();
    }

    /**
     * Busca los campos que están anotados con {@link Inject} en una clase
     * @param clazz la clase target
     * @return los campos anotados, o un set vacío si no tiene ninguno
     */
    private @NotNull Set<Field> getAnnotatedFields(@NotNull Class<?> clazz){
        Set<Field> annotatedFields = new HashSet<>();
        for(Field field : clazz.getDeclaredFields()){
            if(isAnnotatedInjectField(field)){
                int modifiers = field.getModifiers();
                if(Modifier.isFinal(modifiers)) throw new UnsupportedFieldInjectionException("Cannot inject final fields");
                if(Modifier.isStatic(modifiers)) throw new UnsupportedFieldInjectionException("Cannot inject static fields");
                if(Modifier.isTransient(modifiers)) throw new UnsupportedFieldInjectionException("Cannot inject transient fields");
                annotatedFields.add(field);
            }
        }
        return annotatedFields;
    }

    /**
     * Determina si una clase tiene la anotación {@link Bean} o {@link Singleton}
     * @param clazz la clase target
     * @return verdadero si tiene la anotación, falso si no
     */
    public static boolean isAnnotatedBean(Class<?> clazz) {
        Class<?> beanclass = Objects.requireNonNull(clazz, "clazz cannot be null");
        return beanclass.isAnnotationPresent(Bean.class) || beanclass.isAnnotationPresent(Singleton.class);
    }

    public static boolean isBeanAnnotationPresent(Class<?> clazz){
        return Objects.requireNonNull(clazz).isAnnotationPresent(Bean.class);
    }
    /**
     * Determina si una clase tiene la anotación {@link Singleton} o si tiene la {@link Bean} como singleton
     * @param clazz la clase target
     * @return verdadero si tiene la anotación, falso si no
     */
    private static boolean isAnnotatedSingleton(Class<?> clazz) {
        if(isBeanAnnotationPresent(clazz)) {
            Bean annotation = clazz.getAnnotation(Bean.class);
            return annotation.singleton();
        } else {
            return clazz.isAnnotationPresent(Singleton.class);
        }
    }

    /**
     * Determina si un bean debe siempre se debe instance do como Lazy,
     * según el modo de {@link Singleton} o {@link Bean}, y si está presente la anotación {@link Lazy}
     * @param clazz la clase target
     * @return verdadero si es Lazy
     */
    private static boolean isAnnotatedLazy(Class<?> clazz) {
        if(isBeanAnnotationPresent(clazz)) {
            return clazz.getAnnotation(Bean.class).mode().equals(InstantiationMode.LAZY);
        } else if(isAnnotatedSingleton(clazz)) {
            return clazz.getAnnotation(Singleton.class).mode().equals(InstantiationMode.LAZY);
        } else {
            return clazz.isAnnotationPresent(Lazy.class);
        }
    }

    /**
     * Determina si un constructor tiene la anotación {@link Inject}
     * @param constructor el constructor target
     * @return verdadero si tiene la anotación, falso si no
     */
    private static boolean isAnnotatedInjectConstructor(Constructor<?> constructor){
        return Objects.requireNonNull(constructor, "constructor cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Determina si un campo de clase tiene la anotación {@link Inject}
     * @param field el campo de clase target
     * @return verdadero si tiene la anotación, falso si no
     */
    private static boolean isAnnotatedInjectField(Field field) {
        return Objects.requireNonNull(field, "field cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Determina si un parámetro tiene la anotación {@link Inject}
     * @param param el parámetro target
     * @return verdadero si tiene la anotación, falso si no
     */
    private static boolean isAnnotatedInjectParam(Parameter param) {
        return Objects.requireNonNull(param, "param cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Determina si un método tiene la anotación {@link Inject}
     * @param method el método target
     * @return verdadero si tiene la anotación, falso si no
     */
    private static boolean isAnnotatedInjectMethod(Method method) {
        return Objects.requireNonNull(method, "method cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Extrae el nombre de bean de una clase anotada
     * @param clazz la clase target
     * @return el nombre de bean, o un optional vacío si no se declara en las anotaciones
     */
    private static Optional<String> extractAnnotatedBeanName(Class<?> clazz) {
        if(isBeanAnnotationPresent(clazz)) {
            return Optional.ofNullable(clazz.getAnnotation(Bean.class).name());
        } else if(isAnnotatedSingleton(clazz)) {
            return Optional.ofNullable(clazz.getAnnotation(Singleton.class).name());
        }
        return Optional.empty();
    }
}
