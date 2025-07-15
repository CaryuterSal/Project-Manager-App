package dev.builder.core.infrastructure.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Determina que el elemento marcado debe obtener su instancia por medio de la Inyección de Dependencias del contenedor de beans.
 * Esto solo si el escaneo de paquetes está activado
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
