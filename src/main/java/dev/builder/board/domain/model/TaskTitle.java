package dev.builder.board.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Objeto de valor que representa el título de una tarea.
 *
 * El título solo puede contener caracteres alfanuméricos, expacio o símbolos
 */
public record TaskTitle(String value) implements ValueObject<TaskTitle> {

    // Patrón regex para permitir solo letra,  números, espacios o símbolos
    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9 -_]+$");

    /**
     * Crea una nueva instancia de TaskTitle validando su formato.
     *
     * @param value El título de la tarea.
     * @throws NullPointerException Si el título es null.
     * @throws IllegalArgumentException Si el título contiene caracteres no permitidos.
     */
    public TaskTitle {
        validate(value);
    }

    /**
     * Valida que el título no sea null y contenga solo caracteres alfanuméricos, espacios y símbolos.
     *
     * @param value Título a validar.
     * @return El mismo título si es válido.
     * @throws NullPointerException Si el título es null.
     * @throws IllegalArgumentException Si el título contiene caracteres inválidos.
     */
    public static String validate(String value) {
        Objects.requireNonNull(value, "Title must not be null");
        if (!isValid(value)) {
            throw new IllegalArgumentException("Title must only contain alphanumeric characters");
        }
        return value;
    }

    /**
     * Comprueba si el título es válido según el patrón definido.
     *
     * @param value Título a verificar.
     * @return true si es válido, false en caso contrario.
     */
    public static boolean isValid(String value) {
        return value != null && pattern.matcher(value).matches();
    }

    @Override
    public int compareTo(@NotNull TaskTitle taskTitle) {
        return taskTitle.value.compareTo(value);
    }
}
