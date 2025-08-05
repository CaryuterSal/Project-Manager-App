package dev.builder.usermanagement.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.LoggerFactory;

public record CompleteRegistrationCommand(String email, String password) implements Command<UserView> {

    public enum Fields{
        EMAIL("email"),PASSWORD("password");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    @Bean
    public static class CompleteRegistrationCommandValidator extends BaseRequestValidator<CompleteRegistrationCommand> {

        private final EmailValidator emailValidator;
        private final PasswordValidator passwordValidator;

        public CompleteRegistrationCommandValidator(PasswordValidator passwordValidator, EmailValidator emailValidator) {
            super(LoggerFactory.getLogger(CompleteRegistrationCommandValidator.class));
            this.passwordValidator = passwordValidator;
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(CompleteRegistrationCommand value) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.email));
            validate(() -> passwordValidator.validate(Fields.PASSWORD.value, value.password));
            throwIfAny();
        }
    }
}
