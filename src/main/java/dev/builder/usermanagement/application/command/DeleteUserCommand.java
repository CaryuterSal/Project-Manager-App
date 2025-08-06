package dev.builder.usermanagement.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import org.slf4j.LoggerFactory;

public record DeleteUserCommand(String email) implements Command<Void> {

    public enum Fields{
        EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    @Bean
    public static class DeleteUserCommandValidator extends BaseRequestValidator<DeleteUserCommand> {

        private final EmailValidator passwordValidator;

        public DeleteUserCommandValidator( EmailValidator passwordValidator) {
            super(LoggerFactory.getLogger(CompleteRegistrationCommand.CompleteRegistrationCommandValidator.class));
            this.passwordValidator = passwordValidator;
        }

        @Override
        public void validate(DeleteUserCommand value) throws ValidationException {
            validate(() -> passwordValidator.validate(Fields.EMAIL.value, value.email()));
            throwIfAny();
        }
    }
}
