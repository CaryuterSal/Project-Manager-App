package dev.builder.core.infrastructure.di.constructor;

import dev.builder.core.infrastructure.di.exception.ConstructorNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Si no hay constructores candidatos, busca el constructor por defecto (sin parámetros).
 */
public class NoParamConstructorChooser<T> implements ConstructorChooserStrategy<T> {

    @Override
    public Constructor<T> select(Class<T> declaringclass, List<Constructor<T>> candidates) {
        if(!candidates.isEmpty()) {
            for(Constructor<T> constructor : candidates){
                if(constructor.getParameterCount() == 0){
                    constructor.setAccessible(true);
                    return constructor;
                }
            }
        } else {
            try {
                Constructor<T> constructor =  declaringclass.getConstructor();
                if(Modifier.isPrivate(constructor.getModifiers())) throw new NoSuchMethodException();
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "No public constructor found for declaringclass", e);
            }
        };
        return null;
    }
}
