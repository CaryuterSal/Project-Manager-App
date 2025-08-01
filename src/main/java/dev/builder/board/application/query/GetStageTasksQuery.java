package dev.builder.board.application.query;

import dev.builder.board.domain.model.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public final class GetStageTasksQuery{
    private final String boardOwner;
    private final Stage.StageState stage;

    private GetStageTasksQuery(String boardOwner, Stage.StageState stage) {
        this.boardOwner = boardOwner;
        this.stage = stage;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull GetStageTasksQuery forOwner(String boardOwner, Stage.StageState stage) {
        return new GetStageTasksQuery(boardOwner, stage);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull GetStageTasksQuery forOwnBoard(Stage.StageState stage) {
        return new GetStageTasksQuery(null, stage);
    }

    @Contract(pure = true)
    public @NotNull Optional<String> boardOwner() {
        return Optional.ofNullable(boardOwner);
    }

    public Stage.StageState stage() {
        return stage;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GetStageTasksQuery that)) return false;

        return Objects.equals(boardOwner, that.boardOwner) && stage == that.stage;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(boardOwner);
        result = 31 * result + stage.hashCode();
        return result;
    }
}
