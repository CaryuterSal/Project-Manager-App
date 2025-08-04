package dev.builder.board.application.command;

import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Stage;
import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MoveTaskCommand implements Command<BoardView> {

    public enum Fields{
        TASK_ID("taskId"), STAGE("stage");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }


    private final UUID taskId;
    private final Stage.StageState stage;
    private final Boolean placeAtStart;
    private final Boolean placeAtEnd;
    private final UUID previousTask;
    private final UUID nextTask;

    private MoveTaskCommand(UUID taskId, Stage.StageState stage, UUID previousTask, UUID nextTask, Boolean placeAtStart, Boolean placeAtEnd) {
        this.taskId = taskId;
        this.stage = stage;
        this.previousTask = previousTask;
        this.nextTask = nextTask;
        this.placeAtStart = placeAtStart;
        this.placeAtEnd = placeAtEnd;
    }

    public UUID taskId() {
        return taskId;
    }

    public Stage.StageState stage() {
        return stage;
    }

    public Optional<UUID> previousTask() {
        return Optional.ofNullable(previousTask);
    }

    public Optional<UUID> nextTask() {
        return Optional.ofNullable(nextTask);
    }

    public Optional<Boolean> placeAtStart() {
        return Optional.ofNullable(placeAtStart);
    }

    public Optional<Boolean> placeAtEnd() {
        return Optional.ofNullable(placeAtEnd);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MoveTaskCommand that)) return false;

        return taskId.equals(that.taskId) && stage == that.stage && Objects.equals(placeAtStart, that.placeAtStart) && Objects.equals(placeAtEnd, that.placeAtEnd) && Objects.equals(previousTask, that.previousTask) && Objects.equals(nextTask, that.nextTask);
    }

    @Override
    public int hashCode() {
        int result = taskId.hashCode();
        result = 31 * result + stage.hashCode();
        result = 31 * result + Objects.hashCode(placeAtStart);
        result = 31 * result + Objects.hashCode(placeAtEnd);
        result = 31 * result + Objects.hashCode(previousTask);
        result = 31 * result + Objects.hashCode(nextTask);
        return result;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull BuilderStateStage builder(){
        return new Builder();
    }

    public static class Builder implements BuilderTaskIdStage, BuilderStateStage, BuilderEndStage, BuilderBuildStage{
        private UUID taskId;
        private Stage.StageState stage;
        private UUID previousTask;
        private UUID nextTask;
        private Boolean placeAtStart;
        private Boolean placeAtEnd;

        @Override
        public BuilderStateStage task(UUID taskId) {
            this.taskId = taskId;
            return this;
        }

        @Override
        public BuilderEndStage toStage(Stage.StageState stage) {
            this.stage = stage;
            return this;
        }

        @Override
        public BuilderBuildStage placeBefore(UUID nextTask) {
            this.nextTask = nextTask;
            return this;
        }

        @Override
        public BuilderBuildStage placeAfter(UUID previousTask) {
            this.previousTask = previousTask;
            return this;
        }

        @Override
        public MoveTaskCommand placeAtStart() {
            this.placeAtStart = true;
            return build();
        }

        @Override
        public MoveTaskCommand placeAtEnd() {
            this.placeAtEnd = true;
            return build();
        }

        @Override
        public MoveTaskCommand build() {
            return new MoveTaskCommand(taskId, stage, previousTask, nextTask, placeAtStart, placeAtEnd);
        }
    }

    public interface BuilderTaskIdStage{
        BuilderStateStage task(UUID taskId);
    }
    public interface BuilderStateStage{
        BuilderEndStage toStage(Stage.StageState stage);
    }

    public interface BuilderEndStage{
        BuilderBuildStage placeBefore(UUID nextTask);
        BuilderBuildStage placeAfter(UUID previousTask);
        MoveTaskCommand placeAtStart();
        MoveTaskCommand placeAtEnd();
    }

    public interface BuilderBuildStage{
        BuilderBuildStage placeBefore(UUID nextTask);
        BuilderBuildStage placeAfter(UUID previousTask);
        MoveTaskCommand build();
    }

    @Bean
    public static class MoveTaskCommandValidator extends BaseRequestValidator<MoveTaskCommand>{
        private static final Logger log = LoggerFactory.getLogger(MoveTaskCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;

        @Inject
        public MoveTaskCommandValidator(RequiredObjectValidator requiredObjectValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
        }

        @Override
        public void validate(MoveTaskCommand value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId));
            validate(() -> requiredObjectValidator.validate(Fields.STAGE.value, value.stage));
            throwIfAny();
        }
    }
}
