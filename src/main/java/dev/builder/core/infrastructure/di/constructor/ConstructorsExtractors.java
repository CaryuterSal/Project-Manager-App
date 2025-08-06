package dev.builder.core.infrastructure.di.constructor;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class ConstructorsExtractors {

    /**
     * Obtiene todos los constructores de una clase que no son privados
     * @param clazz la clase donde buscar
     * @return la lista de constructores, o vacía si no encuentra ninguno no privado
     * @param <T> el tipo de la clase
     * @throws IllegalArgumentException si la clase no es instanciable (interfaz, enum o clase abstracta)
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Constructor<T>> nonPrivateConstructors(@NotNull Class<T> clazz) {
        int modifiers = clazz.getModifiers();
        if(Modifier.isAbstract(modifiers) || clazz.isInterface() || clazz.isEnum()) {
            throw new IllegalArgumentException("Non instantiable class: " + clazz.getSimpleName());
        }

        return Arrays.stream((Constructor<T>[]) clazz.getDeclaredConstructors())
                .filter(c -> !Modifier.isPrivate(c.getModifiers()))
                .toList();
    }
}
