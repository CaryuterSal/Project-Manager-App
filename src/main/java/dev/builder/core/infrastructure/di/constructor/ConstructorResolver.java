package dev.builder.core.infrastructure.di.constructor;

import java.lang.reflect.Constructor;

/**
 * Dada una clase, selecciona su mejor constructor. Se apoya de los {@link ConstructorChooserStrategy} para su resolución
 * @param <T> el tipo de clase
 */
@FunctionalInterface
public interface ConstructorResolver<T> {

    /**
     * Devuelve el mejor constructor para usar como método de instanciación para la clase
     * @param clazz el tipo de clase
     * @return el mejor constructor, o {@code null} si no hay ninguno que se ajuste a las políticas
     */
    Constructor<T> resolve(Class<T> clazz);
}
