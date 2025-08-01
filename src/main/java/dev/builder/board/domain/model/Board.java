package dev.builder.board.domain.model;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.ValueObject;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.User;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
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
     * Este objeto encapsula un ID de usuario y es utilizado para evitar errores
     * de tipo en el dominio.
     */
    public record Id(Manager.Id userId) implements ValueObject<Id> {

        /**
         * Crea un nuevo identificador de Board en base en un ID de usuario
         * @param userId ID de usuario que representa el ID del Board.
         * @throws IllegalArgumentException Si el UUID es inválido (null).
         */
        public Id {
            validate(userId);
        }

        /**
         * Valida que el UUID proporcionado no sea null.
         *
         * @param userId UUID a validar.
         * @return El mismo UUID si es válido.
         * @throws IllegalArgumentException Si el UUID es null o inválido.
         */
        public static Manager.Id validate(Manager.Id userId) {
            return Objects.requireNonNull(userId);
        }

        /**
         * Verifica si un User.dev.builder.usermanagement.domain.model.User.Id es válido.
         *
         * @param userId User.dev.builder.usermanagement.domain.model.User.Id a verificar.
         * @return true si el User.dev.builder.usermanagement.domain.model.User.Id no es null, false en caso contrario.
         */
        public static boolean isValid(Manager.Id userId) {
            return userId != null;
        }

        @Override
        public int compareTo(@NotNull Board.Id id) {
            return userId().compareTo(id.userId());
        }
    }
}