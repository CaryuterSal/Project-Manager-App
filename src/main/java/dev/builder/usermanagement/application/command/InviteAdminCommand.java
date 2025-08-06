package dev.builder.usermanagement.application.command;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.AdminView;
import org.slf4j.LoggerFactory;

public class InviteAdminCommand extends InviteUserCommand<AdminView> {

    public InviteAdminCommand(String email) {
        super(email);
    }


    @Bean
    public static class InviteAdminCommandValidator extends InviteUserCommandValidator<InviteAdminCommand> {

        @Inject
        public InviteAdminCommandValidator(EmailValidator emailValidator) {
            super(LoggerFactory.getLogger(InviteAdminCommandValidator.class), emailValidator);
        }

        @Override
        protected void validateExtra(InviteAdminCommand command) {
        }
    }
}
