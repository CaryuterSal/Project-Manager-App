package dev.builder.core.infrastructure.di.constructor;

import dev.builder.core.infrastructure.di.exception.ConstructorConflictException;
import dev.builder.core.infrastructure.di.annotation.BeanAnnotationAccessors;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Escoge el constructor que contiene el mayor número de parámetros anotados con {@link dev.builder.core.infrastructure.di.annotation.Inject}.
 * Si hay más de un constructor con el máximo número de parámetros anotados, se lanza un {@link ConstructorConflictException}
 * @param <T> el tipo de la clase que declara el constructor
 */
public class AnnotatedParamsConstructorChooser<T> implements ConstructorChooserStrategy<T> {

    @Override
    public Constructor<T> select(Class<T> declaringClass, List<Constructor<T>> candidates) {
        if(candidates.isEmpty()) return null;

        List<Constructor<T>> annotatedParamConstructors = candidates.stream()
                .filter(c -> BeanAnnotationAccessors.getAnnotatedParamCount(c) != 0)
                .toList();
        if(annotatedParamConstructors.isEmpty()) return null;
        Map<Integer, List<Constructor<T>>> groupedAnnotatedParamConstructors = annotatedParamConstructors.stream()
                .collect(Collectors.groupingBy(BeanAnnotationAccessors::getAnnotatedParamCount));

        NavigableMap<Integer, List<Constructor<T>>> orderedConstructors = new TreeMap<>(groupedAnnotatedParamConstructors).descendingMap();
        List<Constructor<T>> mostSuitableConstructor = orderedConstructors.firstEntry().getValue();
        if(mostSuitableConstructor.size() != 1) throw new ConstructorConflictException(declaringClass, mostSuitableConstructor);

        Constructor<T> constructor = mostSuitableConstructor.getFirst();
        constructor.setAccessible(true);
        return constructor;
    }
}
