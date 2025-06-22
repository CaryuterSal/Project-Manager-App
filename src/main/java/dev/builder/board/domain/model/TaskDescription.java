package dev.builder.board.domain.model;

import dev.builder.core.domain.ValueObject;

import java.util.Objects;

/**
 * Objeto de valor que representa la descripción de una tarea.
 *
 * Actualmente solo valida que la descripción no sea null.
 */
public record TaskDescription(String value) implements ValueObject {

    /**
     * Crea una nueva instancia de TaskDescription validando que el valor no sea null.
     *
     * @param value La descripción de la tarea.
     * @throws NullPointerException Si el valor es null.
     */
    public TaskDescription {
        validate(value);
    }

    /**
     * Valida que la descripción no sea null.
     *
     * @param value Descripción a validar.
     * @return La misma descripción si es válida.
     * @throws NullPointerException Si la descripción es null.
     */
    public static String validate(String value) {
        Objects.requireNonNull(value, "Description must not be null");
        // Se puede agregar validación adicional aquí, por ejemplo longitud mínima, etc.
        return value;
    }

    /**
     * Indica si una descripción es válida.
     *
     * @param value Descripción a verificar.
     * @return true si no es null, false en caso contrario.
     */
    public static boolean isValid(String value) {
        return value != null;
    }
}