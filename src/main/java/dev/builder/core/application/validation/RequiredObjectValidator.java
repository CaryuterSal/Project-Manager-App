package dev.builder.core.application.validation;

import dev.builder.board.application.command.AddAttachmentToTaskCommand;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

@Bean
public class RequiredObjectValidator implements FieldValidator<Object> {

    private final MessageLocalizer messageLocalizer;

    @Inject
    public RequiredObjectValidator(MessageLocalizer messageLocalizer) {
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void validate(String fieldName, Object value) throws FieldViolationException {
        if(value == null){
            throw new RequiredFieldViolation(messageLocalizer, fieldName);
        }
    }
}
