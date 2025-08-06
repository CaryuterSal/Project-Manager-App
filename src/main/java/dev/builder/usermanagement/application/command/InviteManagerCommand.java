package dev.builder.usermanagement.application.command;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.ManagerView;
import org.slf4j.LoggerFactory;

/**
 * Invita a un nuevo manager para que sea parte del sistema. Este tendrá que completar su registro posteriormente usando {@link CompleteRegistrationCommand}.
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>{@link dev.builder.auth.application.service.UnauthorizedException} si la sesión activa no es de {@code Admin}/li>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido</li>
 *       </ul>
 *   </ul>
 */
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
