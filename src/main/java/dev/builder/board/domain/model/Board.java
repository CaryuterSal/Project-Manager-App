package dev.builder.board.domain.model;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.ValueObject;

import java.util.*;
/**
 * Representa un tablero (Board) dentro del sistema, actuando como un Aggregate Root.
 */
public class Board extends AggregateRoot<Board.Id> {

    private final Set<Stage.Id> stageIds = new HashSet<>();

    /**
     * Crea un nuevo Board con un identificador único y un conjunto de etapas asociadas.
     *
     * @param id        Identificador único del tablero (no debe ser null).
     * @param stageIds  Conjunto de identificadores de etapas (no debe ser null ni contener elementos null).
     * @throws NullPointerException  Si alguno de los parámetros son nulos
     */
    public Board(Board.Id id, Set<Stage.Id> stageIds) {
        super(id);
        Objects.requireNonNull(stageIds, "stageIds must not be null");
        stageIds.forEach(Objects::requireNonNull);
        this.stageIds.addAll(stageIds);
    }

    /**
     * Devuelve el conjunto de identificadores de etapas asociadas a este tablero.
     *
     * @return Un conjunto inmodificable de identificadores de etapas.
     */
    public Set<Stage.Id> stageIds() {
        return Collections.unmodifiableSet(stageIds);
    }

    /**
     * Identificador fuertemente tipado para la entidad Board.
     * Este objeto encapsula un UUID válido y es utilizado para evitar errores
     * de tipo en el dominio.
     */
    public record Id(UUID uuid) implements ValueObject {

        /**
         * Crea un nuevo identificador de Board validando que el UUID no sea nulo ni inválido.
         *
         * @param uuid UUID que representa el ID del Board.
         * @throws IllegalArgumentException Si el UUID es inválido (null).
         */
        public Id {
            validate(uuid);
        }

        /**
         * Valida que el UUID proporcionado no sea null.
         *
         * @param uuid UUID a validar.
         * @return El mismo UUID si es válido.
         * @throws IllegalArgumentException Si el UUID es null o inválido.
         */
        public static UUID validate(UUID uuid) {
            if (!isValid(Objects.requireNonNull(uuid))) {
                throw new IllegalArgumentException("Invalid UUID");
            }
            return uuid;
        }

        /**
         * Verifica si un UUID es válido.
         *
         * @param uuid UUID a verificar.
         * @return true si el UUID no es null, false en caso contrario.
         */
        public static boolean isValid(UUID uuid) {
            return uuid != null;
        }
    }
}
