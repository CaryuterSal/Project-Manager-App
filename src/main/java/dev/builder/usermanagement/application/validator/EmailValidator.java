package dev.builder.usermanagement.application.validator;

import dev.builder.core.application.validation.FieldValidator;
import dev.builder.core.application.validation.FieldViolationException;
import dev.builder.core.application.validation.FormatViolation;
import dev.builder.core.application.validation.RequiredFieldValidator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.model.Email;

@Bean
public class EmailValidator implements FieldValidator<String> {

    private final MessageLocalizer messageResolver;
    private final RequiredFieldValidator requiredFieldValidator;

    @Inject
    public EmailValidator(MessageLocalizer messageResolver, RequiredFieldValidator requiredFieldValidator) {
        this.messageResolver = messageResolver;
        this.requiredFieldValidator = requiredFieldValidator;
    }

    @Override
    public void validate(String fieldName, String value) throws FieldViolationException {
        requiredFieldValidator.validate(fieldName, value);
        if(!Email.isValid(value)) {
            throw new FormatViolation(messageResolver, fieldName, value);
        }
    }
}
