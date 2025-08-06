package dev.builder.core.infrastructure.di.constructor;

import java.lang.reflect.Constructor;

class ChainedConstructorResolver<T>  implements ConstructorResolver<T> {

    private final ConstructorResolver<T> first;
    private final ConstructorResolver<T> then;
    public ChainedConstructorResolver(ConstructorResolver<T> first, ConstructorResolver<T> then) {
        this.first = first;
        this.then = then;
    }

    @Override
    public Constructor<T> resolve(Class<T> clazz) {
        Constructor<T> resolved = first.resolve(clazz);
        return resolved == null ? then.resolve(clazz) : resolved;
    }
}
