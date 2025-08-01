package dev.builder.usermanagement.application.query;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.Logger;

public class FindAdminQuery extends FindUserQuery<AdminView> {

    private FindAdminQuery(String email) {
        super(email);
    }

    public static FindAdminQuery forEmail(String email) {
        return new FindAdminQuery(email);
    }

    @Bean
    public static class FindAdminQueryValidator extends FindUserQueryAbstractValidator<FindAdminQuery>{

        @Inject
        public FindAdminQueryValidator(Logger logger, EmailValidator emailValidator) {
            super(logger, emailValidator);
        }

        @Override
        protected void validateExtra() {
        }
    }
}
