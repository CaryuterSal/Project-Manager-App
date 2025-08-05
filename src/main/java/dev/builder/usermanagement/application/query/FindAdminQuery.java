package dev.builder.usermanagement.application.query;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindAdminQuery extends FindUserQuery<AdminView> {

    private FindAdminQuery(String email) {
        super(email);
    }

    public static FindAdminQuery forEmail(String email) {
        return new FindAdminQuery(email);
    }

    @Bean
    public static class FindAdminQueryValidator extends FindUserQueryAbstractValidator<FindAdminQuery>{

        private static final Logger log = LoggerFactory.getLogger(FindAdminQueryValidator.class);

        @Inject
        public FindAdminQueryValidator(EmailValidator emailValidator) {
            super(log, emailValidator);
        }

        @Override
        protected void validateExtra() {
        }
    }
}
