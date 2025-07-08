package dev.builder.core.infrastructure.di.exception;

import dev.builder.core.infrastructure.di.annotation.Singleton;

/**
 * Determina que una anotación de bean ()Ej. {@link Singleton} no está soportada para esta clase.
 * </br>
 * Esto se lanza para clases no instantiable como interfaces
 */
public class UnsupportedAnnotatedBeanException extends RuntimeException {
    public UnsupportedAnnotatedBeanException(String message) {
        super(message);
    }
}
