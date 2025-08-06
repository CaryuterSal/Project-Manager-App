package dev.builder.board.application.command;

import dev.builder.board.application.view.FileView;
import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.Image;
import dev.builder.core.application.Command;
import dev.builder.core.application.validation.*;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;

/**
 * Añade una imágen de portada a la tarea especificada. Devuelve un {@link FileView} con los metadatos del archivo subido
 * </br>
 * Posibles excepciones lanzadas:
 *  <ul>
 *      <li>{@link dev.builder.auth.application.service.UnauthorizedException} Si el usuario activo no es {@code Manager} dueño del tablero o un {@code Student} que colabora en la tarea</li>
 *      <li>{@link dev.builder.board.domain.exception.TaskNotFoundException} si no se encuentra una tarea con dicho ID en el tablero del Manager</li>
 *      <li>{@link IllegalStateException} si la tarea ya tiene una imágen de portada designada</li>
 *      <li>{@link java.io.IOException} si ocurre un error al leer el stream de datos</li>
 *      <li>{@link dev.builder.board.domain.exception.FileUnsupportedException} si el formato de archivo es inválido o no es soportado</li>
 *      <li>Posibles violaciones de validación</li>
 *      <ul>
 *          <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *          <li>{@link dev.builder.core.application.validation.FormatViolation} si el formato de nombre de archivo es inválido</li>
 *      </ul>
 *  </ul>
 * @param taskId el ID de la tarea
 * @param filename el nombre del archivo
 * @param imageStream el archivo en forma de stream
 */
public record AttachCoverImageToTaskCommand(UUID taskId, String filename, InputStream imageStream) implements Command<FileView> {

    public enum Fields{
        TASK_ID("taskId"), FILENAME("filename"), IMAGE_STREAM("imageStream");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }


    @Bean
    public static class AttachCoverImageToTaskCommandValidator extends BaseRequestValidator<AttachCoverImageToTaskCommand> {

        private static final Logger log = LoggerFactory.getLogger(AttachCoverImageToTaskCommandValidator.class);
        private final RequiredFieldValidator requiredValidator;
        private final RequiredObjectValidator requiredObjectValidator;
        private final MessageLocalizer messageLocalizer;

        @Inject
        public AttachCoverImageToTaskCommandValidator(RequiredFieldValidator requiredValidator, RequiredObjectValidator requiredObjectValidator, MessageLocalizer messageLocalizer) {
            super(log);
            this.requiredValidator = requiredValidator;
            this.requiredObjectValidator = requiredObjectValidator;
            this.messageLocalizer = messageLocalizer;
        }

        @Override
        public void validate(AttachCoverImageToTaskCommand value) throws ValidationException {
            validate(() -> requiredValidator.validate(AttachCoverImageToTaskCommand.Fields.FILENAME.value, value.filename));
            validate(() -> requiredObjectValidator.validate(AttachCoverImageToTaskCommand.Fields.TASK_ID.value, value.taskId));
            validate(() -> requiredObjectValidator.validate(Fields.IMAGE_STREAM.value, value.imageStream));
            validate(() -> {
                if(!Image.Filename.isValid(value.filename)) {
                    throw new FormatViolation(messageLocalizer, AttachCoverImageToTaskCommand.Fields.FILENAME.value, value.filename);
                }
            });
            throwIfAny();
        }
    }
}
