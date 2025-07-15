package dev.builder.core.infrastructure.di.annotation;

import dev.builder.core.infrastructure.di.definition.InstantiationMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca que el elemento es un {@link Bean} que debe ser instanciado como patrón singleton (única instancia por aplicación)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {
    String name() default "";
    InstantiationMode mode() default InstantiationMode.LAZY;
}
