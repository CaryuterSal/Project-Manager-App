package dev.builder.board.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequiredFieldValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Elimina una asignación a una tarea, esto elimina los permisos de edición del estudiante a dicha tarea y no permite visualizarla.
 * </br>
 * Posibles excepciones lanzadas:
 *  <ul>
 *      <li>{@link dev.builder.auth.application.service.UnauthorizedException} si no se tiene una sesión activa como {@code Manager}</li>
 *      <li>{@link dev.builder.board.domain.exception.TaskNotFoundException} si no se encuentra una tarea con dicho ID en el tablero del Manager</li>
 *      <li>{@link dev.builder.usermanagement.domain.exception.StudentNotFoundException} si el estudiante no existe o no colabora en el tablero del Manager o no está asignado a dicha tarea</li>
 *      <li>Posibles violaciones de validación</li>
 *      <ul>
 *          <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *          <li>{@link dev.builder.core.application.validation.FormatViolation} si el formato de correo electrónico del estudiante es inválido</li>
 *      </ul>
 *  </ul>
 * @param taskId
 * @param studentEmail
 */
public record RevokeAssignmentFromTaskCommand(UUID taskId, String studentEmail) implements Command<Void> {
    public enum Fields{
        TASK_ID("taskId"), STUDENT_EMAIL("studentEmail");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class RevokeAssignmentFromTaskCommandValidator extends BaseRequestValidator<RevokeAssignmentFromTaskCommand> {
        private static final Logger log = LoggerFactory.getLogger(RevokeAssignmentFromTaskCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;
        private final RequiredFieldValidator requiredFieldValidator;

        @Inject
        public RevokeAssignmentFromTaskCommandValidator( RequiredObjectValidator requiredObjectValidator, RequiredFieldValidator requiredFieldValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
            this.requiredFieldValidator = requiredFieldValidator;
        }

        @Override
        public void validate(RevokeAssignmentFromTaskCommand value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId));
            validate(() -> requiredFieldValidator.validate(Fields.STUDENT_EMAIL.value, value.studentEmail));
            throwIfAny();
        }
    }

}
