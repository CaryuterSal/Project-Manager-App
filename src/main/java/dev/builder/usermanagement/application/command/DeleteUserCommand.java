package dev.builder.usermanagement.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import org.slf4j.LoggerFactory;

/**
 * Elimina la cuenta determinada. Este comando solo está permitido para {@code Admins}
 *  </br>
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>{@link dev.builder.usermanagement.domain.exception.UserNotFoundException} Si no se encuentra un usuario con el correo electrónico</li>
 *       <li>{@link dev.builder.auth.application.service.UnauthorizedException} si no fuiste tu quien creó al usuario a eliminar. Solo los usuarios que invitaron a otros usuarios pueden eliminar a dichos usuarios/li>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido</li>
 *       </ul>
 *   </ul>
 * @param email el correo electrónico del {@code Manager} a eliminar
 */
public record DeleteUserCommand(String email) implements Command<Void> {

    public enum Fields{
        EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    @Bean
    public static class DeleteUserCommandValidator extends BaseRequestValidator<DeleteUserCommand> {

        private final EmailValidator passwordValidator;

        public DeleteUserCommandValidator( EmailValidator passwordValidator) {
            super(LoggerFactory.getLogger(CompleteRegistrationCommand.CompleteRegistrationCommandValidator.class));
            this.passwordValidator = passwordValidator;
        }

        @Override
        public void validate(DeleteUserCommand value) throws ValidationException {
            validate(() -> passwordValidator.validate(Fields.EMAIL.value, value.email()));
            throwIfAny();
        }
    }
}
