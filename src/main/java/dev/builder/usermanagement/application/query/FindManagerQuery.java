package dev.builder.usermanagement.application.query;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.ManagerView;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindManagerQuery extends FindUserQuery<ManagerView> {

    private FindManagerQuery(String email) {
        super(email);
    }

    public static FindManagerQuery forEmail(String email) {
        return new FindManagerQuery(email);
    }

    @Bean
    public static class FindManagerQueryValidator extends FindUserQueryAbstractValidator<FindManagerQuery>{

        private static final Logger log = LoggerFactory.getLogger(FindManagerQueryValidator.class);

        @Inject
        public FindManagerQueryValidator(EmailValidator emailValidator) {
            super(log, emailValidator);
        }

        @Override
        protected void validateExtra() {
        }
    }
}
