package dev.builder.usermanagement.application.query;

import dev.builder.core.application.Sort;
import dev.builder.core.application.SortField;
import dev.builder.core.application.validation.DateRangeValidator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.ManagerView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class FindAllManagersQuery extends  FindAllUsersQuery<ManagerView, FindAllManagersQuery.ManagerSortableField> {


    public enum Fields{
        MIN_CREATED_DATE("minCreatedDate"), MAX_CREATED_DATE("maxCreatedDate"), CREATED_BY("createdBy");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    private final String createdBy;

    private FindAllManagersQuery(Sort<ManagerSortableField> sort,  LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate, String createdBy) {
        super(sort, minCreatedDate, maxCreatedDate);
        this.createdBy = createdBy;
    }

    public Optional<String> createdBy(){
        return Optional.ofNullable(createdBy);
    }

    @Contract(" -> new")
    public static @NotNull FindAllManagersQuery asActive(){
        return new FindAllManagersQuery(null, null, null, null);
    }

    public static class Builder extends  FindAllUsersQuery.Builder<ManagerSortableField> {
        private String createdBy;

        public Builder createdBy(String createdBy){
            this.createdBy = createdBy;
            return this;
        }

        @Override
        public FindAllManagersQuery build(){
            return new FindAllManagersQuery(sort, minCreatedDate, maxCreatedDate, createdBy);
        }
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public enum ManagerSortableField implements SortField {
        EMAIL,
        CREATED_AT,
        UPDATED_AT,
        CREATED_BY
    }


    @Bean
    public static class FindAllManagersQueryValidator extends FindAllUsersQueryAbstractValidator<FindAllManagersQuery> {

        private static final Logger log = LoggerFactory.getLogger(FindAllManagersQueryValidator.class);
        private final EmailValidator emailValidator;

        @Inject
        public FindAllManagersQueryValidator( DateRangeValidator dateValidator, EmailValidator emailValidator) {
            super(log, dateValidator);
            this.emailValidator = emailValidator;
        }

        @Override
        protected void validateExtra(FindAllManagersQuery value) {
            value.createdBy().ifPresent(
                    email -> validate(() -> emailValidator.validate(Fields.CREATED_BY.getValue(), email))
            );
        }
    }

}
