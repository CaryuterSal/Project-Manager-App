package dev.builder.usermanagement.application.validator;

import dev.builder.core.application.validation.FieldValidator;
import dev.builder.core.application.validation.FieldViolationException;
import dev.builder.core.application.validation.RequiredFieldValidator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

@Bean
public class NameValidator implements FieldValidator<String> {

    private final RequiredFieldValidator requiredFieldValidator;

    @Inject
    public NameValidator( RequiredFieldValidator requiredFieldValidator) {
        this.requiredFieldValidator = requiredFieldValidator;
    }

    @Override
    public void validate(String fieldName, String value) throws FieldViolationException {
        requiredFieldValidator.validate(fieldName, value);
    }
}
