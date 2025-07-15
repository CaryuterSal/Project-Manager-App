package dev.builder.core.infrastructure.di.constructor;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Selecciona el mejor constructor dada una lista de ellos
 * @param <T> el tipo de clase del constructor
 */
@FunctionalInterface
public interface ConstructorChooserStrategy<T> {

    /**
     * Resuelve de entre varios candidatos de constructor, el mejor dependiendo de la estrategia.
     * </br>
     * Usa el patrón <a href="https://refactoring.guru/design-patterns/strategy">Strategy</a>
     * </br>
     * Si el constructor existe, se asegura que sea visible para instanciar (se llama {@link Constructor#setAccessible(boolean)}
     * @param declaringClass la clase que declara los constructores
     * @param candidates los constructores de la clase candidatos
     * @return el mejor constructor dependiendo de la estrategia, o {@link null} si la estrategia no aplica
     */
    Constructor<T> select(Class<T> declaringClass, List<Constructor<T>> candidates);
}
