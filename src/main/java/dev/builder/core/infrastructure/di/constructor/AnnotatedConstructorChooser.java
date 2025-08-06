package dev.builder.core.infrastructure.di.constructor;

import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.exception.ConstructorConflictException;
import dev.builder.core.infrastructure.di.annotation.BeanAnnotationAccessors;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Escoge el constructor anotado con {@link Inject}, si hay más de uno lanza un {@link ConstructorConflictException}
 * @param <T> el tipo de la clase que declara el constructor
 */
public class AnnotatedConstructorChooser<T> implements ConstructorChooserStrategy<T> {

    /**
     * @throws ConstructorConflictException si hay más de un constructor anotado con {@link Inject}
     */
    @Override
    public Constructor<T> select(Class<T> declaringClass, List<Constructor<T>> candidates) {
        List<Constructor<T>> annotatedConstructors = BeanAnnotationAccessors.getInjectAnnotatedConstructors(candidates);
        if(annotatedConstructors.isEmpty()) return null;
        if(annotatedConstructors.size() != 1) throw new ConstructorConflictException(declaringClass, candidates);

        Constructor<T> constructor = annotatedConstructors.getFirst();
        constructor.setAccessible(true);
        return constructor;
    }
}
