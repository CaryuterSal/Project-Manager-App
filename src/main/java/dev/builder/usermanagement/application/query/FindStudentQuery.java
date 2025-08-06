package dev.builder.usermanagement.application.query;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Obtiene la información de un estudiante como {@link java.util.Optional} de {@link dev.builder.usermanagement.application.view.StudentView}.
 * Usar el método <b>factory</b> {@code forEmail(String email)}
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido</li>
 *       </ul>
 *   </ul>
 **/
public class FindStudentQuery extends FindUserQuery<StudentView> {

    private FindStudentQuery(String email) {
        super(email);
    }

    public static FindStudentQuery forEmail(String email) {
        return new FindStudentQuery(email);
    }

    @Bean
    public static class FindStudentQueryValidator extends FindUserQueryAbstractValidator<FindStudentQuery>{

        private static final Logger log = LoggerFactory.getLogger(FindStudentQueryValidator.class);

        @Inject
        public FindStudentQueryValidator( EmailValidator emailValidator) {
            super(log, emailValidator);
        }

        @Override
        protected void validateExtra() {
        }
    }
}
