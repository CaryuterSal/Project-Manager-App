package dev.builder.usermanagement.application.command;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.ManagerView;
import org.slf4j.LoggerFactory;

public class InviteManagerCommand extends InviteUserCommand<ManagerView> {

    public InviteManagerCommand(String email) {
        super(email);
    }


    @Bean
    public static class InviteManagerCommandValidator extends InviteUserCommandValidator<InviteManagerCommand> {

        @Inject
        public InviteManagerCommandValidator(EmailValidator emailValidator ) {
            super(LoggerFactory.getLogger(InviteManagerCommandValidator.class), emailValidator);
        }

        @Override
        protected void validateExtra(InviteManagerCommand command) {
        }
    }
}
