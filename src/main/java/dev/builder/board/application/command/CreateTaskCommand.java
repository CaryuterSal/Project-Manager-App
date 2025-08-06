package dev.builder.board.application.command;

import dev.builder.board.application.view.StageView;
import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.TaskDescription;
import dev.builder.board.domain.model.Title;
import dev.builder.core.application.Command;
import dev.builder.core.application.validation.*;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Crea una nueva tarea en una determinada etapa del tablero. Esta nueva tarea se agrega al final de la columna. Devuelve un {@link StageView} con la nueva tarea creada dentro de el, además de las ya existentes
 *
 * </br>
 * Posibles excepciones lanzadas:
 *  <ul>
 *      <li>{@link dev.builder.auth.application.service.UnauthorizedException} Si el usuario activo no es {@code Manager}</li>
 *      <li>Posibles violaciones de validación</li>
 *      <ul>
 *          <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *          <li>{@link dev.builder.core.application.validation.FormatViolation} si el formato de título de la tarea es inválido</li>
 *          <li>{@link DateFutureViolation} si la fecha límite para completar la tarea no se encuentra en el futuro</li>
 *      </ul>
 *  </ul>
 * @param stage la etapa en la que se debe crear la tarea
 * @param title el título de la tarea
 * @param description la descripción de la tarea
 * @param color el color de marcado de la tarea
 * @param deadline la fecha límite para completar la tarea
 */
public record CreateTaskCommand(Stage.StageState stage, String title, String description, Color color, LocalDateTime deadline) implements Command<StageView> {
    public enum Fields{
        STAGE("stage"), TITLE("title"), DESCRIPTION("description"), COLOR("color"), DEADLINE("deadline");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class CreateTaskCommandValidator extends BaseRequestValidator<CreateTaskCommand>{
        private static final Logger log = LoggerFactory.getLogger(CreateTaskCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;
        private final RequiredFieldValidator requiredFieldValidator;
        private final MessageLocalizer messageLocalizer;

        @Inject
        public CreateTaskCommandValidator(RequiredObjectValidator requiredObjectValidator, RequiredFieldValidator requiredFieldValidator, MessageLocalizer messageLocalizer) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
            this.requiredFieldValidator = requiredFieldValidator;
            this.messageLocalizer = messageLocalizer;
        }

        @Override
        public void validate(CreateTaskCommand value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.STAGE.value, value.stage));
            validate(() -> requiredFieldValidator.validate(Fields.TITLE.value, value.title));
            validate(() -> {
                if(!Title.isValid(value.title)){
                    throw new FormatViolation(messageLocalizer, Fields.TITLE.value, value.title);
                }
            });
            validate(() -> requiredFieldValidator.validate(Fields.DESCRIPTION.value, value.description));
            validate(() -> requiredObjectValidator.validate(Fields.COLOR.value, value.color));
            validate(() -> requiredObjectValidator.validate(Fields.DEADLINE.value, value.deadline));
            validate(() ->{
               if(!value.deadline.isAfter(LocalDateTime.now())){
                   throw new DateFutureViolation(messageLocalizer, Fields.DEADLINE.value);
               }
            });
            throwIfAny();
        }
    }
}
