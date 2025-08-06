package dev.builder.board.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.DateFutureViolation;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Elimina un archivo adjunto de una tarea.
 *  </br>
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>{@link dev.builder.auth.application.service.UnauthorizedException} Si el usuario activo no es {@code Manager} o es un {@code Student} que no colabora con dicha tarea</li>
 *       <li>{@link dev.builder.board.domain.exception.TaskNotFoundException} si no se encuentra una tarea con dicho ID en el tablero del Manager</li>
 *       <li>{@link dev.builder.board.domain.exception.FileNotFoundException} si no se encuentra el archivo con el ID indicado</li>
 *
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null}</li>
 *       </ul>
 *   </ul>
 * @param taskId el ID de la tarea
 * @param attachmentId el ID del archivo adjunto
 */
public record RemoveAttachmentCommand(UUID taskId, UUID attachmentId) implements Command<Void> {
    public enum Fields{
        TASK_ID("taskId"), ATTACHMENT_ID("attachmentId");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }


    @Bean
    public static class RemoveAttachmentCommandValidator extends BaseRequestValidator<RemoveAttachmentCommand> {
        private static final Logger log = LoggerFactory.getLogger(RemoveAttachmentCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;

        @Inject
        public RemoveAttachmentCommandValidator(RequiredObjectValidator requiredObjectValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
        }

        @Override
        public void validate(RemoveAttachmentCommand value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId ));
            validate(() -> requiredObjectValidator.validate(Fields.ATTACHMENT_ID.value, value.attachmentId));
            throwIfAny();
        }
    }

}
