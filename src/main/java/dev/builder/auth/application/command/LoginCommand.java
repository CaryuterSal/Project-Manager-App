package dev.builder.auth.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LoginCommand(String email, String password) implements Command<Void> {

    public enum Fields{
        EMAIL("email"), PASSWORD("password");
        private final String value;
        Fields(String value){this.value = value;}
        public String getValue(){return value;}
    }

    @Bean
    public static class LoginCommandValidator extends BaseRequestValidator<LoginCommand>{

        private static final Logger log = LoggerFactory.getLogger(LoginCommandValidator.class);
        private final EmailValidator emailValidator;
        private final PasswordValidator passwordValidator;

        @Inject
        public LoginCommandValidator(EmailValidator emailValidator, PasswordValidator passwordValidator) {
            super(log);
            this.emailValidator = emailValidator;
            this.passwordValidator = passwordValidator;
        }

        @Override
        public void validate(LoginCommand value) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.email()));
            validate(() -> passwordValidator.validate(Fields.PASSWORD.value, value.password()));
            throwIfAny();
        }
    }
}
