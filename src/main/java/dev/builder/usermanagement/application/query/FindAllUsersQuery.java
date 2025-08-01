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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FindAllUsersQuery<T extends UserView, S extends  Enum<S> & SortField> implements Query<List<T>> {

    public enum Fields{
        MIN_CREATED_DATE("minCreatedDate"), MAX_CREATED_DATE("maxCreatedDate");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    private final Sort<S> sort;
    private final LocalDateTime minCreatedDate;
    private final LocalDateTime maxCreatedDate;

    protected FindAllUsersQuery(Sort<S> sort, LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
        this.sort = sort;
        this.minCreatedDate = minCreatedDate;
        this.maxCreatedDate = maxCreatedDate;
    }

    public static FindAllUsersQuery<?,?> asActive(){
        return new FindAllUsersQuery<>(null,  null, null);
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
    public static <S extends Enum<S> & SortField> @NotNull Builder<S> builder() {
        return new Builder<>();
    }

    public static class Builder<S extends Enum<S> & SortField> {
        protected Sort<S> sort;
        protected LocalDateTime minCreatedDate;
        protected LocalDateTime maxCreatedDate;

        public Builder<S> sort(Sort<S> sort) {
            this.sort = sort;
            return this;
        }

        public Builder<S> minCreatedDate(LocalDateTime minCreatedDate) {
            this.minCreatedDate = minCreatedDate;
            return this;
        }

        public Builder<S> maxCreatedDate(LocalDateTime maxCreatedDate) {
            this.maxCreatedDate = maxCreatedDate;
            return this;
        }

        public Builder<S> createdRange(LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
            this.minCreatedDate = minCreatedDate;
            this.maxCreatedDate = maxCreatedDate;
            return this;
        }

        public FindAllUsersQuery<?,S> build() {
            return new  FindAllUsersQuery<>(sort,minCreatedDate, maxCreatedDate);
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

        @Inject
        public FindAllUsersQueryValidator(Logger logger, DateRangeValidator dateValidator) {
            super(logger, dateValidator);
        }

        @Override
        protected void validateExtra(FindAllUsersQuery<UserView, UserSortableField> value) {
        }
    }
}
