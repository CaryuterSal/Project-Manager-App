package dev.builder.usermanagement.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.LoggerFactory;

public record CompleteRegistrationCommand(String password) implements Command<UserView> {

    public enum Fields{
        PASSWORD("password");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    @Bean
    public static class CompleteRegistrationCommandValidator extends BaseRequestValidator<CompleteRegistrationCommand> {

        private final PasswordValidator passwordValidator;

        public CompleteRegistrationCommandValidator( PasswordValidator passwordValidator) {
            super(LoggerFactory.getLogger(CompleteRegistrationCommandValidator.class));
            this.passwordValidator = passwordValidator;
        }

        @Override
        public void validate(CompleteRegistrationCommand value) throws ValidationException {
            validate(() -> passwordValidator.validate(Fields.PASSWORD.value, value.password));
            throwIfAny();
        }
    }
}
