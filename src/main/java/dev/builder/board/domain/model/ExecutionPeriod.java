package dev.builder.board.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa un período de ejecución de una tarea o acción definido por una fecha y hora de inicio y
 * una fecha y hora de fin, ambos de tipo {@link LocalDateTime}.
 *
 * Este Value Object asegura que el período sea válido: la fecha de inicio debe
 * ser anterior o igual a la fecha de fin (si esta última es proporcionada).
 *
 * Puede representar períodos abiertos, donde la fecha de fin es null. Esto también puede interpretarse como un periodo que aún no ha tenido fin
 */
public record ExecutionPeriod(LocalDateTime startTime, LocalDateTime endTime) implements ValueObject {

    /**
     * Constructor auxiliar para crear un período con solo fecha de inicio.
     * El período queda abierto (sin fecha de fin).
     *
     * @param start Fecha y hora de inicio (no puede ser null).
     */
    public ExecutionPeriod(LocalDateTime start) {
        this(start, null);
    }

    /**
     * Constructor principal que valida el período y asegura la inmutabilidad.
     *
     * @param startTime Fecha y hora de inicio (no puede ser null).
     * @param endTime Fecha y hora de fin (puede ser null para período abierto).
     * @throws IllegalArgumentException si el período no es válido.
     */
    public ExecutionPeriod {
        validate(startTime, endTime);
    }

    /**
     * Devuelve un nuevo {@code ExecutionPeriod} con la fecha de inicio especificada,
     * manteniendo la fecha de fin actual.
     *
     * @param startTime Nueva fecha de inicio (no puede ser null).
     * @return Nuevo objeto {@code ExecutionPeriod} con la fecha de inicio actualizada.
     */
    @Contract("_ -> new")
    public @NotNull ExecutionPeriod withStart(LocalDateTime startTime) {
        return new ExecutionPeriod(startTime, endTime);
    }

    /**
     * Devuelve un nuevo {@code ExecutionPeriod} con la fecha de fin especificada,
     * manteniendo la fecha de inicio actual.
     *
     * @param endTime Nueva fecha de fin (puede ser null para período abierto).
     * @return Nuevo objeto {@code ExecutionPeriod} con la fecha de fin actualizada.
     */
    @Contract("_ -> new")
    public @NotNull ExecutionPeriod withEnd(LocalDateTime endTime) {
        return new ExecutionPeriod(startTime, endTime);
    }

    /**
     * Obtiene la fecha y hora de inicio del período.
     *
     * @return Fecha y hora de inicio.
     */
    public LocalDateTime start() {
        return startTime;
    }

    /**
     * Obtiene la fecha y hora de fin del período si está presente.
     *
     * @return {@link Optional} con la fecha y hora de fin, o vacío si es período abierto.
     */
    @Contract(pure = true)
    public @NotNull Optional<LocalDateTime> end() {
        return Optional.ofNullable(endTime);
    }

    /**
     * Valida que el período representado por start y end sea válido,
     * es decir, que start no sea posterior a end (cuando end no es null).
     *
     * @param start Fecha y hora de inicio (no puede ser null).
     * @param end Fecha y hora de fin (puede ser null).
     * @throws IllegalArgumentException si el período no es válido.
     * @throws NullPointerException si start es null.
     */
    public static void validate(LocalDateTime start, LocalDateTime end) {
        if (!isValid(Objects.requireNonNull(start), end)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    /**
     * Indica si el período definido por start y end es válido.
     * El start debe ser no null y antes o igual que end (o end null para período abierto).
     *
     * @param start Fecha y hora de inicio (no puede ser null).
     * @param end Fecha y hora de fin (puede ser null).
     * @return {@code true} si el período es válido, {@code false} en caso contrario.
     */
    public static boolean isValid(LocalDateTime start, LocalDateTime end) {
        return start != null && (end == null || start.isBefore(end) || start.isEqual(end));
    }

    /**
     * Calcula la duración del período entre start y end en la unidad temporal dada.
     *
     * @param unit Unidad temporal para medir la duración (ejemplo: segundos, minutos).
     * @return La duración entre start y end en la unidad especificada.
     * @throws NullPointerException si end es null o unit es null.
     */
    public long duration(TemporalUnit unit) {
        Objects.requireNonNull(endTime, "End time must not be null to calculate duration");
        Objects.requireNonNull(unit, "TemporalUnit must not be null");
        return startTime.until(endTime, unit);
    }

    /**
     * Determina si un punto temporal dado está dentro del rango (start, end),
     * es decir, estrictamente posterior a start y anterior a end.
     *
     * @param point Fecha y hora a evaluar.
     * @return {@code true} si el punto está dentro del período, {@code false} en caso contrario.
     * @throws NullPointerException si point es null.
     */
    public boolean isInRange(LocalDateTime point) {
        Objects.requireNonNull(point);
        return startTime.isBefore(point) && endTime != null && endTime.isAfter(point);
    }
}
