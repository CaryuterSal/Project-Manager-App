package dev.builder.core.infrastructure.di.constructor;

import dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Obtiene el constructor de una clase que más parámetros tiene
 * @param <T>
 */
public class ParameterCountConstructorResolver<T> implements ConstructorResolver<T> {

    private final ConstructorChooserStrategy<T> fallbackStrategy;
    private final ConstructorChooserStrategy<T> paramCountChooser = new ParameterCountConstructorChooser<>();


    /**
     * Usa {@link NoParamConstructorChooser} como fallback por defecto
     */
    public ParameterCountConstructorResolver() {
        this.fallbackStrategy = new NoParamConstructorChooser<>();
    }
    public ParameterCountConstructorResolver(ConstructorChooserStrategy<T> fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
    }
    /**
     * Encuentra el constructor con más parámetros de cierta clase, lo que lo hace el más correcto para inyección de dependencias.
     * Este es completamente accesible
     * @param clazz la clase target
     * @return El mejor candidato de constructor, o {@code null} si ninguno existe
     */
    @Override
    public Constructor<T> resolve(Class<T> clazz) {
        List<Constructor<T>> constructors = ConstructorsExtractors.nonPrivateConstructors(clazz);
        try{
            if(constructors.isEmpty()) throw new ConstructorNotFoundException(clazz);
            Constructor<T> largestConstructor = paramCountChooser.select(clazz, constructors);
            if(largestConstructor == null)  throw new ConstructorNotFoundException(clazz);
            return largestConstructor;
        } catch (ConstructorNotFoundException e){
            return fallbackStrategy == null ? null : fallbackStrategy.select(clazz, constructors);
        }
    }
}
