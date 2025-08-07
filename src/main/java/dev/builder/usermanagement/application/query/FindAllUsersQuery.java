package dev.builder.usermanagement.application.query;

import dev.builder.core.application.Query;
import dev.builder.core.application.Sort;
import dev.builder.core.application.SortField;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.DateRangeValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.UserView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Obtiene la información de varios usuarios como {@link java.util.List} de {@link dev.builder.usermanagement.application.view.UserView}.
 * Usar el método <b>factory</b> {@code builder()}
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido</li>
 *       </ul>
 *   </ul>
 **/
public class FindAllUsersQuery<T extends UserView, S extends  Enum<S> & SortField> implements Query<List<T>> {

    public static class FindAllUsersGenericQuery extends FindAllUsersQuery<UserView, FindAllUsersQuery.UserSortableField> {
        private FindAllUsersGenericQuery(Sort<UserSortableField> sort, String emailLike,  LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
            super(sort, emailLike, minCreatedDate, maxCreatedDate);
        }

        @Contract(value = " -> new", pure = true)
        public static @NotNull FindAllUsersGenericQuery.Builder builder() {
            return new FindAllUsersGenericQuery.Builder();
        }


        public static class Builder extends FindAllUsersQuery.Builder<Builder, FindAllUsersQuery.UserSortableField> {
            @Override
            public FindAllUsersQuery<?, UserSortableField> build() {
                return new FindAllUsersGenericQuery(super.sort, super.emailLike, super.minCreatedDate, super.maxCreatedDate);
            }

            @Override
            protected Builder getSelf() {
                return super.getSelf();
            }
        }
    }
    public enum Fields{
        MIN_CREATED_DATE("minCreatedDate"), MAX_CREATED_DATE("maxCreatedDate");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    private final Sort<S> sort;
    private final LocalDateTime minCreatedDate;
    private final LocalDateTime maxCreatedDate;
    private final String emailLike;

    protected FindAllUsersQuery(Sort<S> sort, String emailLike, LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
        this.sort = sort;
        this.emailLike = emailLike;
        this.minCreatedDate = minCreatedDate;
        this.maxCreatedDate = maxCreatedDate;
    }

    public static FindAllUsersQuery<?,?> asActive(){
        return new FindAllUsersQuery<>(null,  null,null, null);
    }

    public Optional<Sort<S>> sort() {
        return Optional.ofNullable(sort);
    }

    public Optional<LocalDateTime> minCreatedDate() {
        return Optional.ofNullable(minCreatedDate);
    }

    public Optional<LocalDateTime> maxCreatedDate() {
        return Optional.ofNullable(maxCreatedDate);
    }

    public Optional<String> emailLike() {return Optional.ofNullable(emailLike);}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        FindAllUsersQuery<?, ?> that = (FindAllUsersQuery<?, ?>) o;
        return Objects.equals(sort, that.sort) && Objects.equals(minCreatedDate, that.minCreatedDate) && Objects.equals(maxCreatedDate, that.maxCreatedDate);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(sort);
        result = 31 * result + Objects.hashCode(minCreatedDate);
        result = 31 * result + Objects.hashCode(maxCreatedDate);
        return result;
    }

    @Contract(value = " -> new", pure = true)
    public static <S extends Enum<S> & SortField> @NotNull Builder<?,S> builder() {
        return new Builder<>();
    }

    public static class Builder<SELF extends Builder<SELF, S>, S extends Enum<S> & SortField> {
        protected Sort<S> sort;
        protected LocalDateTime minCreatedDate;
        protected LocalDateTime maxCreatedDate;
        protected String emailLike;

        @SuppressWarnings("unchecked")
        protected SELF getSelf(){
            return (SELF) this;
        };
        public SELF sort(Sort<S> sort) {
            this.sort = sort;
            return getSelf();
        }

        public SELF emailLike(String emailLike) {
            this.emailLike = emailLike;
            return getSelf();
        }

        public SELF minCreatedDate(LocalDateTime minCreatedDate) {
            this.minCreatedDate = minCreatedDate;
            return getSelf();
        }

        public SELF maxCreatedDate(LocalDateTime maxCreatedDate) {
            this.maxCreatedDate = maxCreatedDate;
            return getSelf();
        }

        public SELF createdRange(LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
            this.minCreatedDate = minCreatedDate;
            this.maxCreatedDate = maxCreatedDate;
            return getSelf();
        }

        public FindAllUsersQuery<?,S> build() {
            return new FindAllUsersQuery<>(sort,emailLike,minCreatedDate, maxCreatedDate);
        }
    }

    public enum UserSortableField implements SortField {
        EMAIL,
        CREATED_AT,
        UPDATED_AT
    }


    public static abstract class FindAllUsersQueryAbstractValidator<T extends FindAllUsersQuery<?,?>> extends BaseRequestValidator<T>{

        private final DateRangeValidator dateValidator;
        public FindAllUsersQueryAbstractValidator(Logger logger, DateRangeValidator dateValidator) {
            super(logger);
            this.dateValidator = dateValidator;
        }

        @Override
        public void validate(T value) throws ValidationException {
            if(value.minCreatedDate().isPresent() && value.maxCreatedDate().isPresent()){
                validate(() -> dateValidator.validate(Fields.MIN_CREATED_DATE.getValue(), Fields.MAX_CREATED_DATE.getValue(),value.minCreatedDate().get(), value.maxCreatedDate().get()));
            }
            validateExtra(value);
            throwIfAny();
        }

        protected abstract void validateExtra(T value);
    }

    @Bean
    public static class FindAllUsersQueryValidator extends FindAllUsersQueryAbstractValidator<FindAllUsersQuery<UserView,UserSortableField>> {

        private static final Logger log = LoggerFactory.getLogger(FindAllUsersQueryValidator.class);

        @Inject
        public FindAllUsersQueryValidator( DateRangeValidator dateValidator) {
            super(log, dateValidator);
        }

        @Override
        protected void validateExtra(FindAllUsersQuery<UserView, UserSortableField> value) {
        }
    }
}
