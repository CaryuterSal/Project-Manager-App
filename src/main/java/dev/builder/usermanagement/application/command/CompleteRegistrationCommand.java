package dev.builder.usermanagement.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.LoggerFactory;

/**
 * Completa el proceso de verificación de una cuenta invitada por medio de {@link InviteUserCommand}, {@link InviteManagerCommand}, o {@link InviteStudentCommand}.
 *
 * En caso de que la petición se procese con éxito, automáticamente:
 * <ul>
 *     <li>Si el tipo de usuario es {@code Manager}, se crea un tablero único para el</li>
 *     <li>Si el tipo de usuario es {@code Student}, automáticamente se asigna como colaborador del Manager que lo invitó</li>
 * </ul>
 *  </br>
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>{@link dev.builder.usermanagement.domain.exception.UserNotFoundException} Si no se encuentra un usuario con el correo electrónico</li>
 *       <li>{@link IllegalStateException} si el usuario ya está verificado</li>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido o la contraseña es tan débil que es inválida</li>
 *       </ul>
 *   </ul>
 * @param email el correo electrónico de la cuenta por verificar
 * @param password la nueva contraseña sin encriptar de la cuenta por verificar
 */
public record CompleteRegistrationCommand(String email, String password) implements Command<UserView> {

    public enum Fields{
        EMAIL("email"),PASSWORD("password");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    @Bean
    public static class CompleteRegistrationCommandValidator extends BaseRequestValidator<CompleteRegistrationCommand> {

        private final EmailValidator emailValidator;
        private final PasswordValidator passwordValidator;

        public CompleteRegistrationCommandValidator(PasswordValidator passwordValidator, EmailValidator emailValidator) {
            super(LoggerFactory.getLogger(CompleteRegistrationCommandValidator.class));
            this.passwordValidator = passwordValidator;
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(CompleteRegistrationCommand value) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.email));
            validate(() -> passwordValidator.validate(Fields.PASSWORD.value, value.password));
            throwIfAny();
        }
    }
}
