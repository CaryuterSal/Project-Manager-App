package dev.builder.board.application.query;

import dev.builder.board.application.view.BoardView;
import dev.builder.core.application.Query;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Obtiene la información del tablero en forma de {@link Optional<BoardView>}, o un Optional vacío si no existe el tablero especificado
 * </br>
 * <b>Usar los <a href="https://refactoring.guru/design-patterns/factory-method">Factory</a> existentes</b>.
 *
 */
public final class GetBoardQuery implements Query<Optional<BoardView>> {

    private final String boardOwner;

    private GetBoardQuery(String boardOwner) {
        this.boardOwner = boardOwner;
    }

    /**
     * Busca el tablero de un determinado {@code Manager}. Uso esperado para los {@code Student}
     * @param boardOwner el correo electrónico del Manager
     * @return query configurada para el tablero del Manager
     */
    @Contract("_ -> new")
    public static @NotNull GetBoardQuery forOwner(String boardOwner) {
        Objects.requireNonNull(boardOwner);
        return new GetBoardQuery(boardOwner);
    }

    /**
     * Busca la información del {@code Manager} con sesión activa
     * @return query configurada para el tablero del Manager
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull GetBoardQuery own(){
        return new GetBoardQuery(null);
    }

    @Contract(pure = true)
    public @NotNull Optional<String> boardOwner() {
        return Optional.ofNullable(boardOwner);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GetBoardQuery that)) return false;

        return Objects.equals(boardOwner, that.boardOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(boardOwner);
    }
}
