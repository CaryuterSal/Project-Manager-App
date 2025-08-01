package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

@Bean
public class PositiveValidator implements FieldValidator<Integer>{
    private final MessageLocalizer messageLocalizer;

    @Inject
    public PositiveValidator(MessageLocalizer messageLocalizer) {
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void validate(String fieldName, Integer value) throws FieldViolationException {
        if(value == null) {
            throw new RequiredFieldViolation(messageLocalizer, fieldName);
        }
        if(value <= 0) {
            throw new NumberPositiveViolation(messageLocalizer, fieldName);
        }
    }
}
