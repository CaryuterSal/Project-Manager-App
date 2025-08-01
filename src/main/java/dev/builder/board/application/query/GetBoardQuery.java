package dev.builder.board.application.query;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public final class GetBoardQuery {

    private final String boardOwner;

    private GetBoardQuery(String boardOwner) {
        this.boardOwner = boardOwner;
    }

    @Contract("_ -> new")
    public static @NotNull GetBoardQuery forOwner(String boardOwner) {
        Objects.requireNonNull(boardOwner);
        return new GetBoardQuery(boardOwner);
    }

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
