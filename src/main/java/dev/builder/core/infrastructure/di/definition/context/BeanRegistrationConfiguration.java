package dev.builder.core.infrastructure.di.definition.context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Define la configuración que usa el {@link dev.builder.core.infrastructure.di.DependencyContainer} para registrar un nuevo bean
 * @param <T> el tipo de bean
 */
public final class BeanRegistrationConfiguration<T>{
    private final Class<T> clazz;
    private final String beanName;
    private final BeanScope beanScope;
    private final InitCustomizer<T> initCustomizer;
    private final InstantiationMode instantiationMode;

    BeanRegistrationConfiguration(Class<T> clazz, String beanName, BeanScope beanScope, InitCustomizer<T> initCustomizer, InstantiationMode instantiationMode) {
        this.clazz = clazz;
        this.beanName = beanName;
        this.beanScope = beanScope;
        this.initCustomizer = initCustomizer;
        this.instantiationMode = instantiationMode;
    }

    public Class<T> clazz() {
        return clazz;
    }

    public BeanScope beanScope() {
        return beanScope;
    }

    @Contract(pure = true)
    public @NotNull Optional<String> beanName() {
        return Optional.ofNullable(beanName);
    }

    @Contract(pure = true)
    public @NotNull Optional<InitCustomizer<T>> initCustomizer() {
        return Optional.ofNullable(initCustomizer);
    }

    @Contract(pure = true)
    public @NotNull Optional<InstantiationMode> instatiationMode() {
        return Optional.ofNullable(instantiationMode);
    }
}
