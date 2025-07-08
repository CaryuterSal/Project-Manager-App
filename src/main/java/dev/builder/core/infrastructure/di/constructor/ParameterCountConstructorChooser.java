package dev.builder.core.infrastructure.di.constructor;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;

/**
 * Devuelve el constructor con el mayor número de parámetros
 * @param <T> eñ tipo del constructor
 */
public class ParameterCountConstructorChooser<T> implements ConstructorChooserStrategy<T> {

    @Override
    public Constructor<T> select(Class<T> declaringClass, List<Constructor<T>> candidates) {
        if (candidates.isEmpty()) return null;
        Constructor<T> bestCandidate = candidates
                .stream()
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .get();
        bestCandidate.setAccessible(true);
        return bestCandidate;
    }
}
