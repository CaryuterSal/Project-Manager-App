package dev.builder.usermanagement.application.query;

import dev.builder.core.application.Sort;
import dev.builder.core.application.validation.DateRangeValidator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.view.AdminView;
import dev.builder.usermanagement.application.view.UserView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;


/**
 * Obtiene la información de varios administradores como {@link java.util.List} de {@link dev.builder.usermanagement.application.view.AdminView}.
 * Usar el método <b>Builder/b> {@code builder()}
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido</li>
 *       </ul>
 *   </ul>
 **/
public class FindAllAdminsQuery extends FindAllUsersQuery<AdminView, FindAllUsersQuery.UserSortableField> {

    private FindAllAdminsQuery(Sort<UserSortableField> sort, String emailLike, LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
        super(sort, emailLike, minCreatedDate, maxCreatedDate);
    }

    @Contract(" -> new")
    public static @NotNull FindAllAdminsQuery asActive(){
        return new FindAllAdminsQuery(null, null,null, null);
    }

    @Contract(value = " -> new", pure = true)
    public static FindAllAdminsQuery.@NotNull Builder builder() {
        return new FindAllAdminsQuery.Builder();
    }


    public static class Builder extends  FindAllUsersQuery.Builder<Builder,UserSortableField> {

        @Override
        protected Builder getSelf() {
            return this;
        }

        @Override
        public FindAllAdminsQuery build(){
            return new FindAllAdminsQuery(sort,  emailLike,minCreatedDate, maxCreatedDate);
        }
    }


    @Bean
    public static class FindAllAdminsQueryValidator extends FindAllUsersQueryAbstractValidator<FindAllAdminsQuery> {

        private static final Logger log = LoggerFactory.getLogger(FindAllAdminsQueryValidator.class);

        @Inject
        public FindAllAdminsQueryValidator(DateRangeValidator dateValidator) {
            super(log, dateValidator);
        }

        @Override
        protected void validateExtra(FindAllAdminsQuery value) {

        }
    }

}
