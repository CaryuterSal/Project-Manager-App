package dev.builder.core.application;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Define un órden en el que se ordenan los resultados de una {@link Query}
 * @param field el campo sobre el que se va a ordenar
 * @param asc verdadero si el ordenamiento debe ser en órden ascendente
 */
public record Sort<T extends Enum<T> & SortField>(T field, boolean asc) {

    /**
     * Crea un ordenamiento ascendente
     * @param field el campo sobre el que se va a ordenar
     * @return un ordenamiento
     */
    @Contract("_ -> new")
    public static <T extends Enum<T> & SortField> @NotNull Sort<T> asc(T field) {
        return new Sort<>(field, true);
    }


    /**
     * Crea un ordenamiento descendente
     * @param field el campo sobre el que se va a ordenar
     * @return un ordenamiento
     */
    @Contract("_ -> new")
    public static <T extends Enum<T> & SortField> @NotNull Sort<T> desc(T field) {
        return new Sort<>(field, false);
    }
}
