package dev.builder.board.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequestValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Añade un nuevo estudiante como colaborador a tu tablero (requiere sesión como {@link dev.builder.auth.infrastructure.Role} {@code Manager}. Requiere que el estudiante ya exista.
 * </br>
 *  Posibles excepciones lanzadas:
 *  <ul>
 *      <li>{@link dev.builder.auth.application.service.UnauthorizedException} si no se tiene una sesión activa como {@code Manager}</li>
 *      <li>{@link dev.builder.usermanagement.domain.exception.StudentNotFoundException} si el estudiante no existe</li>
 *      <li>Posibles violaciones de validación</li>
 *      <ul>
 *          <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si el correo electrónico es {@code null} o solo contiene espacios</li>
 *          <li>{@link dev.builder.core.application.validation.FormatViolation} si el formato de correo electrónico es inválido</li>
 *      </ul>
 *  </ul>
 * @param email el correo electrónico del estudiante a añadir.
 */
public record AddCollaboratorCommand(String email) implements Command<Void> {
    public enum Fields{
        EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class AddCollaboratorCommandValidator extends BaseRequestValidator<AddCollaboratorCommand>{
        private static final Logger log = LoggerFactory.getLogger(AddCollaboratorCommandValidator.class);
        private final EmailValidator emailValidator;

        @Inject
        public AddCollaboratorCommandValidator(EmailValidator emailValidator) {
            super(log);
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(AddCollaboratorCommand value) throws ValidationException {
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.email));
            throwIfAny();
        }
    }
}
