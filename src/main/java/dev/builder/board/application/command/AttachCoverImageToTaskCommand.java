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
