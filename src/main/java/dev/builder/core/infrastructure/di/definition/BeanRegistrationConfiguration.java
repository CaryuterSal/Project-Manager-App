package dev.builder.core.infrastructure.di.definition;

import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Define la configuración que usa el {@link DependencyContainer} para registrar un nuevo bean
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

    @Contract(pure = true)
    BeanRegistrationConfiguration(@NotNull BeanRegistrationConfiguration<T> self){
        this.clazz = self.clazz;
        this.beanName = self.beanName;
        this.beanScope = self.beanScope;
        this.initCustomizer = self.initCustomizer;
        this.instantiationMode = self.instantiationMode;
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

    /**
     * Añade un nuevo callback de customizer antes del que ya estaba definido
     * @param customizer el nuevo callback
     * @return nueva configuración
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull BeanRegistrationConfiguration<T> prependInitCustomizer(InitCustomizer<T> customizer){
        return withInitCustomizer((bean) -> {
            customizer.accept(bean);
            initCustomizer().ifPresent(c -> c.accept(bean));
        });
    }

    /**
     * Añade un nuevo callback de customizer después del que ya estaba definido
     * @param customizer el nuevo callback
     * @return nueva configuración
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull BeanRegistrationConfiguration<T> appendInitCustomizer(InitCustomizer<T> customizer){
        return withInitCustomizer((bean) -> {
            initCustomizer().ifPresent(c -> c.accept(bean));
            customizer.accept(bean);
        });
    }

    /**
     * Crea una nueva configuración con el {@link InitCustomizer} especificado
     * @param customizer el personalizer especificado
     * @return nueva configuración
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull BeanRegistrationConfiguration<T> withInitCustomizer(InitCustomizer<T> customizer){
        return new BeanRegistrationConfiguration<>(clazz, beanName, beanScope, customizer, instantiationMode);
    }

    @Contract(pure = true)
    public @NotNull Optional<InstantiationMode> instatiationMode() {
        return Optional.ofNullable(instantiationMode);
    }

    public static <T> @NotNull FirstStep<T> builder(Class<T> clazz){
        if(clazz == null) throw new NullPointerException("clazz is null");
        return new BeanRegistrationBuilder<>(clazz);
    }
}
