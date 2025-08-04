package dev.builder.usermanagement.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class InviteUserCommand<T extends UserView> implements Command<T> {
    public enum Fields{
        EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    private final String email;

    public InviteUserCommand(String email) {
        this.email = email;
    }

    public String email() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        InviteUserCommand<?> that = (InviteUserCommand<?>) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    protected static abstract class InviteUserCommandValidator<T extends InviteUserCommand<?>> extends BaseRequestValidator<T> {
        private final EmailValidator emailValidator;

        @Inject
        public InviteUserCommandValidator(Logger log, EmailValidator emailValidator) {
            super(log);
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(T command) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.getValue(),command.email()));
            validateExtra(command);
            throwIfAny();
        }

        protected abstract void validateExtra(T command);
    }
}
