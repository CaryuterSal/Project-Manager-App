package dev.builder.board.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record AddCollaboratorCommand(String email) implements Command<Void> {
    public enum Fields{
        EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class AddCollaboratorCommandValidator extends BaseRequestValidator<AddCollaboratorCommand>{
        private static final Logger log = LoggerFactory.getLogger(AddCollaboratorCommandValidator.class);
        private final EmailValidator emailValidator;

        @Inject
        public AddCollaboratorCommandValidator(EmailValidator emailValidator) {
            super(log);
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(AddCollaboratorCommand value) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.email));
            throwIfAny();
        }
    }
}
