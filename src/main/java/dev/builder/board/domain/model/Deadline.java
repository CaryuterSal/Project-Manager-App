package dev.builder.board.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * Representa una fecha y hora límite (deadline) en el dominio, asegurando que
 * siempre sea una fecha futura respecto al momento actual.
 *
 * Este Value Object encapsula una instancia de {@link LocalDateTime} y valida
 * que su valor sea posterior al tiempo presente en el momento de creación.
 */
public record Deadline(LocalDateTime value) implements ValueObject<Deadline> {

    /**
     * Crea una nueva instancia de {@code Deadline}, validando que el valor proporcionado
     * sea posterior al momento actual.
     *
     * @param value Fecha y hora límite. No puede ser null ni una fecha pasada o presente.
     * @throws NullPointerException Si {@code value} es null.
     * @throws IllegalArgumentException Si {@code value} no es posterior al momento actual.
     */
    public Deadline {
        validate(value);
    }

    /**
     * Valida que la fecha y hora dada sea una fecha futura respecto al momento actual.
     *
     * @param value La fecha y hora a validar.
     * @return La misma fecha y hora si es válida.
     * @throws NullPointerException Si {@code value} es null.
     * @throws IllegalArgumentException Si {@code value} no es una fecha futura.
     */
    public static LocalDateTime validate(LocalDateTime value) {
        if (!isValid(Objects.requireNonNull(value))) {
            throw new IllegalArgumentException("Deadline must be after present");
        }
        return value;
    }

    /**
     * Determina si la fecha y hora dada es válida, es decir, si es posterior
     * al momento actual.
     *
     * @param value La fecha y hora a evaluar.
     * @return {@code true} si la fecha es posterior a la actual, {@code false} en caso contrario.
     */
    public static boolean isValid(LocalDateTime value) {
        return value != null;
    }

    /**
     * Calcula el tiempo restante desde un punto temporal dado hasta la fecha límite,
     * en la unidad temporal especificada.
     *
     * @param point El punto temporal desde el que se calcula el tiempo restante.
     * @param unit La unidad temporal para medir la diferencia (ejemplo: segundos, minutos).
     * @return El tiempo restante desde {@code point} hasta la fecha límite en la unidad especificada.
     */
    @Contract(pure = true)
    public long timeLeft(@NotNull LocalDateTime point, TemporalUnit unit) {
        return point.until(value, unit);
    }

    /**
     * Determina si un punto temporal dado ocurre antes que la fecha límite.
     *
     * @param point El punto temporal a comparar.
     * @return {@code true} si {@code point} es anterior a la fecha límite, {@code false} en caso contrario.
     */
    @Contract(pure = true)
    public boolean isBefore(@NotNull LocalDateTime point) {
        return point.isBefore(value);
    }

    @Override
    public int compareTo(@NotNull Deadline deadline) {
        return value.compareTo(deadline.value);
    }
}
