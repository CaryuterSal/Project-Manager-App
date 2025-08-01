package dev.builder.usermanagement.application.query;

import dev.builder.core.application.Query;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.UserView;
import dev.builder.usermanagement.domain.model.User;
import org.slf4j.Logger;

import java.util.Optional;

public class FindUserQuery<T extends UserView> implements Query<Optional<T>> {
    public enum Fields{
        EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }
    private final String email;

    protected FindUserQuery(String email) {
        this.email = email;
    }

    public static FindUserQuery<UserView> generic(String email) {
        return new FindUserQuery<>(email);
    }

    public String email() {
        return email;
    }

    public static abstract class FindUserQueryAbstractValidator<T extends FindUserQuery<?>> extends BaseRequestValidator<T>  {

        private final EmailValidator emailValidator;

        public FindUserQueryAbstractValidator(Logger logger, EmailValidator emailValidator) {
            super(logger);
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(T value) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.getValue(), value.email()));
            validateExtra();
            throwIfAny();
        }

        protected abstract void validateExtra();
    }

    @Bean
    public static class FindUserQueryValidator extends FindUserQueryAbstractValidator<FindUserQuery<UserView>>{

        @Inject
        public FindUserQueryValidator(Logger logger, EmailValidator emailValidator) {
            super(logger, emailValidator);
        }

        @Override
        protected void validateExtra() {
        }
    }
}
