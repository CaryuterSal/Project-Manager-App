package dev.builder.board.application.command;

import dev.builder.board.application.view.FileView;
import dev.builder.board.application.view.TaskView;

import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.StoredFile;
import dev.builder.core.application.Command;
import dev.builder.core.application.validation.*;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.InputStream;
import java.util.UUID;

public record AddAttachmentToTaskCommand(UUID taskId, String filename, InputStream fileStream) implements Command<FileView> {


    public enum Fields{
        TASK_ID("taskId"), FILENAME("filename"), INPUT_STREAM("fileStream");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }feat: add board application service

    @Bean
    public static class AddAttachmentToTaskCommandValidator extends BaseRequestValidator<AddAttachmentToTaskCommand> {

        private static final Logger log = LoggerFactory.getLogger(AddAttachmentToTaskCommandValidator.class);
        private final RequiredFieldValidator requiredValidator;
        private final RequiredObjectValidator requiredObjectValidator;
        private final MessageLocalizer messageLocalizer;


        @Inject
        public AddAttachmentToTaskCommandValidator(RequiredFieldValidator requiredValidator, RequiredObjectValidator requiredObjectValidator, MessageLocalizer messageLocalizer) {
            super(log);
            this.requiredValidator = requiredValidator;
            this.requiredObjectValidator = requiredObjectValidator;
            this.messageLocalizer = messageLocalizer;
        }

        @Override
        public void validate(AddAttachmentToTaskCommand value) throws ValidationException {
            validate(() -> requiredValidator.validate(Fields.FILENAME.value, value.filename));
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId));
            validate(() -> requiredObjectValidator.validate(Fields.INPUT_STREAM.value, value.fileStream));
            validate(() -> {
                if(!Attachment.Filename.isValid(value.filename)) {
                    throw new FormatViolation(messageLocalizer, Fields.FILENAME.value, value.filename);
                }
            });
            throwIfAny();
        }
    }
}
