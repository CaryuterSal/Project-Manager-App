package dev.builder.board.application.command;

import dev.builder.board.application.view.StageView;
import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Elimina una tarea del tablero. Devuelve el {@link StageView} que previamente contenía la tarea que fue eliminada
 * </br>
 * Posibles excepciones lanzadas:
 *  <ul>
 *      <li>{@link dev.builder.auth.application.service.UnauthorizedException} Si el usuario activo no es {@code Manager}</li>
 *      <li>{@link dev.builder.board.domain.exception.TaskNotFoundException} si no se encuentra una tarea con dicho ID en el tablero del Manager</li>
 *      <li>Posibles violaciones de validación</li>
 *      <ul>
 *          <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si el ID de la tarea es {@code null}</li>
 *      </ul>
 *  </ul>
 * @param taskId el ID de la tarea
 */
public record DeleteTaskCommand(UUID taskId) implements Command<StageView> {
    public enum Fields{
        TASK_ID("taskId");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class DeleteTaskCommandValidator extends BaseRequestValidator<DeleteTaskCommand>{
        private static final Logger log = LoggerFactory.getLogger(DeleteTaskCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;

        @Inject
        public DeleteTaskCommandValidator(RequiredObjectValidator requiredObjectValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
        }

        @Override
        public void validate(DeleteTaskCommand value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId));
            throwIfAny();
        }
    }
}
