package dev.builder.usermanagement.application.validator;

import dev.builder.core.application.validation.FieldValidator;
import dev.builder.core.application.validation.FieldViolationException;
import dev.builder.core.application.validation.FormatViolation;
import dev.builder.core.application.validation.RequiredFieldValidator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.model.Password;

@Bean
public class PasswordValidator implements FieldValidator<String> {

    private final MessageLocalizer messageLocalizer;
    private final RequiredFieldValidator requiredFieldValidator;

    @Inject
    public PasswordValidator(MessageLocalizer messageLocalizer, RequiredFieldValidator requiredFieldValidator) {
        this.messageLocalizer = messageLocalizer;
        this.requiredFieldValidator = requiredFieldValidator;
    }

    @Override
    public void validate(String fieldName, String value) throws FieldViolationException {
        requiredFieldValidator.validate(fieldName, value);
        if(!Password.isValid(value)) {
            throw new FormatViolation(messageLocalizer, fieldName, value);
        }
    }
}
