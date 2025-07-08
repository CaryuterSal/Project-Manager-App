package dev.builder.core.infrastructure.di.annotation;

import dev.builder.core.infrastructure.di.definition.InstantiationMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca que la clase debe ser registrada como bean, esto si el escaneo del paquete está activado
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean{

    public boolean singleton() default true;
    public String name() default "";
    public InstantiationMode mode() default InstantiationMode.EAGER;
}
