package dev.builder.core.infrastructure.di.constructor;

import dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException;

import java.lang.reflect.Constructor;

/**
 * Usa el patrón <a href="https://refactoring.guru/design-patterns/decorator">Decorator</a> o "wrapper" alrededor de una estrategia de resolución de constructor
 * para hacer que falle cuando no se encuentra ninguno.
 * </br> Cuando falla lanza un {@link ConstructorNotFoundException}
 * @param <T>
 */
public class FaillingConstructorResolver<T> implements ConstructorResolver<T> {

    private final ConstructorResolver<T> constructorResolver;

    public FaillingConstructorResolver(ConstructorResolver<T> constructorResolver) {
        this.constructorResolver = constructorResolver;
    }

    /**
     * @throws ConstructorNotFoundException si el resolvedor de constructores base devuelve {@code null}
     */
    @Override
    public Constructor<T> resolve(Class<T> clazz) {
        Constructor<T> selected = constructorResolver.resolve(clazz);
        if(selected == null){
            throw new ConstructorNotFoundException(clazz);
        }
        return selected;
    }
}
