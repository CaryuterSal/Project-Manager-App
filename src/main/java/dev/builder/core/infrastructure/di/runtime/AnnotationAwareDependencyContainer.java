package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.annotation.*;
import dev.builder.core.infrastructure.di.constructor.AnnotationConstructorResolver;
import dev.builder.core.infrastructure.di.constructor.ConstructorResolver;
import dev.builder.core.infrastructure.di.constructor.FaillingConstructorResolver;
import dev.builder.core.infrastructure.di.definition.*;
import dev.builder.core.infrastructure.di.exception.BeanNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public class AnnotationAwareDependencyContainer extends AbstractDependencyContainer{

    private AnnotationAwareDependencyContainer() {}

    private static class InstanceHolder{
        private static final AnnotationAwareDependencyContainer INSTANCE = new AnnotationAwareDependencyContainer();
    }
    /**
     * Getter de la clase usando el patrón <a href=https://refactoring.guru/es/design-patterns/singleton>Singleton</a>.
     * </br>
     * Es <b>Thread-safe</b>
     * @return La instancia de la clase
     */
    public static AnnotationAwareDependencyContainer getInstance(){
        return InstanceHolder.INSTANCE;
    }

    /**
     * @throws BeanNotFoundException si algún bean, ya sea por inyección de campo o constructor no puede inyectarse
     * @throws dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException si no se encuentra un constructor para instance el bean
     */
    @Override
    public void scanPackage(String packageName) {
        BeanPackageScanner beanPackageScanner = new BeanPackageScanner(packageName);
        beanPackageScanner.scan()
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

        OptionalConfigStep<T> configStep = BeanAnnotationAccessors.isAnnotatedSingleton(clazz)
                ? (BeanAnnotationAccessors.isAnnotatedEager(clazz)
                ? BeanRegistrationConfiguration.builder(clazz).asEagerSingleton()
                : BeanRegistrationConfiguration.builder(clazz).asLazySingleton())
                : BeanRegistrationConfiguration.builder(clazz).prototype();

        BeanRegistrationConfiguration<T> config;
        Optional<String> beanName = BeanAnnotationAccessors.extractAnnotatedBeanName(clazz);
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
        Set<Field> injectableFields = BeanAnnotationAccessors.getAnnotatedFields(sourceConfig.clazz());
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

    @Override
    protected <T> ConstructorResolver<T> getConstructorResolver() {
        return new FaillingConstructorResolver<>(
                new AnnotationConstructorResolver<>()
        );
    }
}
