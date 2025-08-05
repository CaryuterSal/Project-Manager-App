package dev.builder.board.application.command;

import dev.builder.board.application.view.TaskView;
import dev.builder.board.domain.model.Color;
import dev.builder.board.domain.model.Title;
import dev.builder.core.application.Command;
import dev.builder.core.application.validation.*;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

public record EditTaskCommand(UUID id, String title, String description, Color color, LocalDateTime deadline) implements Command<TaskView> {

    public enum Fields{
        TITLE("title"), DESCRIPTION("description"), COLOR("color"), DEADLINE("deadline");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }


    @Bean
    public static class EditTaskCommandValidator extends BaseRequestValidator<EditTaskCommand> {
        private static final Logger log = LoggerFactory.getLogger(EditTaskCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;
        private final RequiredFieldValidator requiredFieldValidator;
        private final MessageLocalizer messageLocalizer;

        @Inject
        public EditTaskCommandValidator( RequiredObjectValidator requiredObjectValidator, RequiredFieldValidator requiredFieldValidator, MessageLocalizer messageLocalizer) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
            this.requiredFieldValidator = requiredFieldValidator;
            this.messageLocalizer = messageLocalizer;
        }

        @Override
        public void validate(EditTaskCommand value) throws ValidationException {
            validate(() -> requiredFieldValidator.validate(EditTaskCommand.Fields.TITLE.value, value.title));
            validate(() -> {
                if(!Title.isValid(value.title)){
                    throw new FormatViolation(messageLocalizer, EditTaskCommand.Fields.TITLE.value, value.title);
                }
            });
            validate(() -> requiredFieldValidator.validate(EditTaskCommand.Fields.DESCRIPTION.value, value.description));
            validate(() -> requiredObjectValidator.validate(EditTaskCommand.Fields.COLOR.value, value.color));
            validate(() -> requiredObjectValidator.validate(EditTaskCommand.Fields.DEADLINE.value, value.deadline));
            validate(() ->{
                if(!value.deadline.isAfter(LocalDateTime.now())){
                    throw new DateFutureViolation(messageLocalizer, EditTaskCommand.Fields.DEADLINE.value);
                }
            });
            throwIfAny();
        }
    }
}
