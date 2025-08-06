package dev.builder.board.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Asigna a un estudiante para que realice una tarea. Este estudiante debe ser colaborador del tablero
 * </br>
 * Posibles excepciones lanzadas:
 *  <ul>
 *      <li>{@link dev.builder.auth.application.service.UnauthorizedException} si no se tiene una sesión activa como {@code Manager}</li>
 *      <li>{@link dev.builder.board.domain.exception.TaskNotFoundException} si no se encuentra una tarea con dicho ID en el tablero del Manager</li>
 *      <li>{@link dev.builder.usermanagement.domain.exception.StudentNotFoundException} si el estudiante no existe o no colabora en el tablero del Manager</li>
 *      <li>Posibles violaciones de validación</li>
 *      <ul>
 *          <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *          <li>{@link dev.builder.core.application.validation.FormatViolation} si el formato de correo electrónico del estudiante es inválido</li>
 *      </ul>
 *  </ul>
 * @param taskId el ID de la tarea
 * @param studentEmail el correo electrónico del estudiante
 */
public record AssignStudentToTaskCommand(UUID taskId, String studentEmail) implements Command<Void> {
    public enum Fields{
        TASK_ID("taskId"), EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class AssignStudentToTaskCommandValidator extends BaseRequestValidator<AssignStudentToTaskCommand>{
        private static final Logger log = LoggerFactory.getLogger(AssignStudentToTaskCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;
        private final EmailValidator emailValidator;

        @Inject
        public AssignStudentToTaskCommandValidator( RequiredObjectValidator requiredObjectValidator, EmailValidator emailValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(AssignStudentToTaskCommand value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId));
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.studentEmail));
            throwIfAny();
        }
    }
}
