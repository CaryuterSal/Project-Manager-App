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

import java.time.LocalDateTime;

public class FindAllAdminsQuery extends FindAllUsersQuery<AdminView, FindAllUsersQuery.UserSortableField> {

    private FindAllAdminsQuery(Sort<UserSortableField> sort, LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
        super(sort, minCreatedDate, maxCreatedDate);
    }

    @Contract(" -> new")
    public static @NotNull FindAllAdminsQuery asActive(){
        return new FindAllAdminsQuery(null, null, null);
    }

    @Contract(value = " -> new", pure = true)
    public static FindAllAdminsQuery.@NotNull Builder builder() {
        return new FindAllAdminsQuery.Builder();
    }


    public static class Builder extends  FindAllUsersQuery.Builder<UserSortableField> {
        @Override
        public FindAllAdminsQuery build(){
            return new FindAllAdminsQuery(sort,  minCreatedDate, maxCreatedDate);
        }
    }


    @Bean
    public static class FindAllAdminsQueryValidator extends FindAllUsersQueryAbstractValidator<FindAllAdminsQuery> {

        @Inject
        public FindAllAdminsQueryValidator(Logger logger, DateRangeValidator dateValidator) {
            super(logger, dateValidator);
        }

        @Override
        protected void validateExtra(FindAllAdminsQuery value) {

        }
    }

}
