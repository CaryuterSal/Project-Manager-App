package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

@Bean
public class RequiredFieldValidator implements FieldValidator<String> {
    private final MessageLocalizer messageLocalizer;

    @Inject
    public RequiredFieldValidator(MessageLocalizer messageLocalizer) {
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void validate(String fieldName, String value) throws FieldViolationException {
        if(value == null || value.isEmpty()) {
            throw new RequiredFieldViolation(messageLocalizer, fieldName);
        }
    }
}
