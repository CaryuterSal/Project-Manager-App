package dev.builder.board.application.command;

import dev.builder.board.application.view.BoardView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Stage;
import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.DateFutureViolation;
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

/**
 * Comando para mover una tarea de su posición actual en la columna (etapa) actual del tablero. Devuelve un {@link BoardView} con el estado de todas las columnas después del movimiento de la tarea
 * Ejemplos de uso son:
 * <ul>
 *     <li>Mover la tarea de órden (moverla internamente dentro de la misma columna)</li>
 *     <li>Mover la tarea a una columna vacía</li>
 *     <li>Mover la tarea a otra columna y posicionarla entre dos tareas</li>
 * </ul>
 * </br>
 * En caso de que la tarea estuviera originalmente en {@link dev.builder.board.domain.model.Stage.StageState} {@code TO_DO} y se moviera de columna, se registra la fecha de inicio de la tarea como el día, hora y minutos actuaes.
 * En caso de que la tarea se moviera a {@code DONE}, se registra el día, hora, y minutos de finalización de la tarea.
 * Una tarea que está en {@code DONE} no puede ser movida.
 * </br>
 * <b>Usar el {@code Builder} para construir este comando</b>
 * </br></br>
 * Posibles excepciones lanzadas:
 *  <ul>
 *      <li>{@link dev.builder.auth.application.service.UnauthorizedException} Si el usuario activo no es {@code Manager} o un {@code Student} que colabora en las tareas involucradas en el proceso/li>
 *      <li>{@link dev.builder.board.domain.exception.TaskNotFoundException} si no se encuentra alguna de las tareas involucradas en el proceso en el tablero del Manager</li>
 *      <li>Posibles violaciones de validación</li>
 *      <ul>
 *          <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si el ID de la tarea a mover es {@code null}</li>
 *      </ul>
 *  </ul>
 */
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
    public static @NotNull BuilderTaskIdStage builder(){
        return new Builder();
    }

    /**
     * Builder para construir instancias de {@link MoveTaskCommand}.
     *
     * Este builder sigue un patrón fluido con etapas para establecer:
     * - El ID de la tarea a mover.
     * - El estado del escenario destino.
     * - La posición relativa (antes o después de otra tarea, o al inicio/final).
     *
     * Uso típico:
     * <pre>{@code
     * MoveTaskCommand command = MoveTaskCommand.builder()
     *     .task(taskId)
     *     .toStage(StageState.TODO)
     *     .placeBefore(otherTaskId)
     *     .build();
     * }</pre>
     */
    public static class Builder implements BuilderTaskIdStage, BuilderStateStage, BuilderEndStage, BuilderBuildStage{
        private UUID taskId;
        private Stage.StageState stage;
        private UUID previousTask;
        private UUID nextTask;
        private Boolean placeAtStart;
        private Boolean placeAtEnd;

        /**
         * Especifica el ID de la tarea que se va a mover.
         *
         * @param taskId el identificador único de la tarea
         * @return la siguiente etapa del builder para establecer el estado destino
         */
        @Override
        public BuilderStateStage task(UUID taskId) {
            this.taskId = taskId;
            return this;
        }

        /**
         * Especifica el estado destino al que se moverá la tarea.
         *
         * @param stage el estado del escenario destino (ej. TODO, IN_PROGRESS, DONE)
         * @return la siguiente etapa del builder para ubicar la posición
         */
        @Override
        public BuilderEndStage toStage(Stage.StageState stage) {
            this.stage = stage;
            return this;
        }

        /**
         * Indica que la tarea debe colocarse antes de la tarea con el ID proporcionado.
         *
         * @param nextTask el ID de la tarea siguiente
         * @return la etapa de construcción final del builder
         */
        @Override
        public BuilderBuildStage placeBefore(UUID nextTask) {
            this.nextTask = nextTask;
            return this;
        }

        /**
         * Indica que la tarea debe colocarse después de la tarea con el ID proporcionado.
         *
         * @param previousTask el ID de la tarea anterior
         * @return la etapa de construcción final del builder
         */
        @Override
        public BuilderBuildStage placeAfter(UUID previousTask) {
            this.previousTask = previousTask;
            return this;
        }

        /**
         * Indica que la tarea debe colocarse al inicio de la columna de destino.
         *
         * @return la instancia final de {@link MoveTaskCommand}
         */
        @Override
        public MoveTaskCommand placeAtStart() {
            this.placeAtStart = true;
            return build();
        }

        /**
         * Indica que la tarea debe colocarse al final de la columna de destino.
         *
         * @return la instancia final de {@link MoveTaskCommand}
         */
        @Override
        public MoveTaskCommand placeAtEnd() {
            this.placeAtEnd = true;
            return build();
        }

        /**
         * Construye una instancia de {@link MoveTaskCommand} con los valores configurados.
         *
         * @return nueva instancia de MoveTaskCommand
         */
        @Override
        public MoveTaskCommand build() {
            return new MoveTaskCommand(taskId, stage, previousTask, nextTask, placeAtStart, placeAtEnd);
        }
    }

    /**
     * Etapa del builder que define la tarea a mover.
     */
    public interface BuilderTaskIdStage{
        BuilderStateStage task(UUID taskId);
    }

    /**
     * Etapa del builder que define la columna a la que se va a mover la tarea
     */
    public interface BuilderStateStage{
        BuilderEndStage toStage(Stage.StageState stage);
    }

    /**
     * Etapa del builder que define la posición dentro de la columna a la que se va a mover la tarea
     */
    public interface BuilderEndStage{
        BuilderBuildStage placeBefore(UUID nextTask);
        BuilderBuildStage placeAfter(UUID previousTask);
        MoveTaskCommand placeAtStart();
        MoveTaskCommand placeAtEnd();
    }

    /**
     * Etapa del builder que construye la instancia de {@link MoveTaskCommand}
     */
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
