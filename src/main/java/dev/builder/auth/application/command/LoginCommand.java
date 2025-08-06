package dev.builder.auth.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comando para iniciar sesión con usuario y contraseña.
 * Carga la información de sesión en el {@link dev.builder.auth.domain.port.out.SessionContext}.
 * Se puede verificar si el usuario existe previamente usando {@link dev.builder.usermanagement.application.query.FindUserQuery}
 *
 * </br>
 * Además, la información de sesión se persiste en un archivo encriptado que persiste aunque se cierre la aplicación, la cual se puede recuperar usando {@link RestoreSessionCommand}
 * </br> Se pueden lanzar las siguientes excepciones
 *  <ul>
 *     <li>{@link dev.builder.auth.application.service.UnauthorizedException} Si las credenciales de inicio de sesión son inválidas o no existe un usuario con dicho correo electrónico</li>
 *      <li>Las violaciones de validación pueden ser de tipo:</li>
 *         <ul>
 *                 <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si alguno de los campos está vacío o solo contiene espacios</li>
 *                 <li>{@link dev.builder.core.application.validation.FormatViolation} si el formato de correo electrónico es incorrecto o la contraseña es muy débil</li>
 *         </ul>
 *  </ul>
 * @param email el correo electrónico
 * @param password la contraseña
 *
 */
public record LoginCommand(String email, String password) implements Command<Void> {

    public enum Fields{
        EMAIL("email"), PASSWORD("password");
        private final String value;
        Fields(String value){this.value = value;}
        public String getValue(){return value;}
    }

    @Bean
    public static class LoginCommandValidator extends BaseRequestValidator<LoginCommand>{

        private static final Logger log = LoggerFactory.getLogger(LoginCommandValidator.class);
        private final EmailValidator emailValidator;
        private final PasswordValidator passwordValidator;

        @Inject
        public LoginCommandValidator(EmailValidator emailValidator, PasswordValidator passwordValidator) {
            super(log);
            this.emailValidator = emailValidator;
            this.passwordValidator = passwordValidator;
        }

        @Override
        public void validate(LoginCommand value) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.email()));
            validate(() -> passwordValidator.validate(Fields.PASSWORD.value, value.password()));
            throwIfAny();
        }
    }
}
