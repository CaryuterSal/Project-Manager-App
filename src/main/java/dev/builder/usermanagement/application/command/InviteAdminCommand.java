package dev.builder.usermanagement.application.command;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.AdminView;
import org.slf4j.LoggerFactory;

/**
 * Invita a un nuevo administrator para que sea parte del sistema. Este tendrá que completar su registro usando {@link CompleteRegistrationCommand}.
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido</li>
 *       </ul>
 *   </ul>
 */
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
