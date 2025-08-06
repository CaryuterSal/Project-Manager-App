package dev.builder.core.infrastructure.di.constructor;

import dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;


/**
 * Escoge el mejor constructor candidato para la inyección de dependencias para la determinada clase.
 * La estrategia que se sigue es la siguiente:
 * </br>
 * <ol>
 * <li>Si no hay ningún constructor se devuelve nulo </li>
 * <li>Si hay constructores anotados con {@link dev.builder.core.infrastructure.di.annotation.Inject} se escoge el que tenga más parámetros</li>
 * <li>Si no, si hay constructores con parámetros anotados con {@link dev.builder.core.infrastructure.di.annotation.Inject} se escoge el que tenga más parámetros</li>
 * <li>Si no, se escoge el constructor con más parámetros, o el constructor vacío</li>
 * </ol>
 * @param <T> el tipo de la clase que declara el constructor
 */
public class AnnotationConstructorResolver<T> implements ConstructorResolver<T> {

    private final ConstructorChooserStrategy<T> fallbackStrategy;
    private final ConstructorChooserStrategy<T> annotatedConstructorChooser = new AnnotatedConstructorChooser<>();
    private final ConstructorChooserStrategy<T> annotatedParamConstructorResolver = new AnnotatedParamsConstructorChooser<>();

    /**
     * Usa {@link NoParamConstructorChooser} como fallback por defecto
     */
    public AnnotationConstructorResolver() {
        this.fallbackStrategy = new NoParamConstructorChooser<>();
    }

    public AnnotationConstructorResolver(ConstructorChooserStrategy<T> fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
    }

    @Override
    public Constructor<T> resolve(Class<T> clazz) {
        List<Constructor<T>> constructors = ConstructorsExtractors.nonPrivateConstructors(clazz);
        try{
            if (constructors.isEmpty()) throw new ConstructorNotFoundException(clazz);

            Constructor<T> annotatedConstructor = annotatedConstructorChooser.select(clazz,constructors);
            if(annotatedConstructor != null) return annotatedConstructor;

            Constructor<T> annotatedParamConstructor = annotatedParamConstructorResolver.select(clazz, constructors);
            if(annotatedParamConstructor == null) throw new ConstructorNotFoundException(clazz);
            return annotatedParamConstructor;
        } catch(ConstructorNotFoundException e) {
            return fallbackStrategy == null ? null : fallbackStrategy.select(clazz, constructors);
        }
    }
}
