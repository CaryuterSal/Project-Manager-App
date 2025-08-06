package dev.builder.usermanagement.application.query;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.UserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Obtiene la información de un administrador como {@link java.util.Optional} de {@link dev.builder.usermanagement.application.view.AdminView}.
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
public class FindAdminQuery extends FindUserQuery<AdminView> {

    private FindAdminQuery(String email) {
        super(email);
    }

    /**
     * Busca un administrador según el correo especificado
     * @param email el correo electrónico
     * @return la query
     */
    public static FindAdminQuery forEmail(String email) {
        return new FindAdminQuery(email);
    }

    @Bean
    public static class FindAdminQueryValidator extends FindUserQueryAbstractValidator<FindAdminQuery>{

        private static final Logger log = LoggerFactory.getLogger(FindAdminQueryValidator.class);

        @Inject
        public FindAdminQueryValidator(EmailValidator emailValidator) {
            super(log, emailValidator);
        }

        @Override
        protected void validateExtra() {
        }
    }
}
