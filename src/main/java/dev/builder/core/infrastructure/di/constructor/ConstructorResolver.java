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

    /**
     * Encadena métodos de resolución de constructores, uno después de otro.
     * Es un tipo de <a href="https://refactoring.guru/design-patterns/builder">Builder</a> para el
     * @param resolverAfter el método de resolución que se ejecuta después de ese
     * @return un nuevo método de resolución que encadena ambos constructores
     */
    default ConstructorResolver<T> fallback(ConstructorResolver<T> resolverAfter) {
        return new ChainedConstructorResolver<>(this, resolverAfter);
    }
}
