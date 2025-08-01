package dev.builder.usermanagement.application.query;

import dev.builder.core.application.Sort;
import dev.builder.core.application.SortField;
import dev.builder.core.application.validation.DateRangeValidator;
import dev.builder.core.application.validation.FormatViolation;
import dev.builder.core.application.validation.PositiveValidator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.view.StudentView;
import dev.builder.usermanagement.domain.model.QuarterGroup;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Optional;

public class FindAllStudentsQuery extends FindAllUsersQuery<StudentView, FindAllStudentsQuery.StudentSortableField> {


    public enum Fields{
        MIN_CREATED_DATE("minCreatedDate"), MAX_CREATED_DATE("maxCreatedDate"), ACADEMIC_GROUP("academicGroup"), ACADEMIC_QUARTER("academicQuarter"), CREATED_BY("createdBy");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    private final Sort<StudentSortableField> sort;
    private final Character academicGroup;
    private final Integer academicQuarter;
    private final String createdBy;

    private FindAllStudentsQuery(Sort<StudentSortableField> sort, LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate, Character academicGroup, Integer academicQuarter, String createdBy) {
        super(null, minCreatedDate, maxCreatedDate);
        this.academicGroup = academicGroup;
        this.academicQuarter = academicQuarter;
        this.createdBy = createdBy;
        this.sort = sort;
    }

    @Override
    public Optional<Sort<StudentSortableField>> sort() {
        return Optional.ofNullable(sort);
    }

    public Optional<Character> academicGroup() {
        return Optional.ofNullable(academicGroup);
    }

    public Optional<Integer> academicQuarter() {
        return Optional.ofNullable(academicQuarter);
    }

    public Optional<String> createdBy() {
        return Optional.ofNullable(createdBy);
    }

    @Contract(" -> new")
    public static @NotNull FindAllStudentsQuery asActive(){
        return new FindAllStudentsQuery(null, null, null, null, null, null);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull FindAllStudentsQuery.Builder builder() {
        return new FindAllStudentsQuery.Builder();
    }

    public static class Builder extends FindAllUsersQuery.Builder<StudentSortableField> {
        private Character academicGroup;
        private Integer academicQuarter;
        private String createdBy;

        public Builder academicGroup(char academicGroup) {
            this.academicGroup = academicGroup;
            return this;
        }

        public Builder academicQuarter(int academicQuarter) {
            this.academicQuarter = academicQuarter;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        @Override
        public FindAllStudentsQuery build() {
            return new FindAllStudentsQuery(sort,minCreatedDate,maxCreatedDate,academicGroup,academicQuarter,createdBy);
        }
    }

    public enum StudentSortableField implements SortField {
        EMAIL,
        CREATED_AT,
        UPDATED_AT,
        NAME,
        QUARTER,
        GROUP,
        CREATED_BY
    }


    @Bean
    public static class FindAllManagersQueryValidator extends FindAllUsersQueryAbstractValidator<FindAllStudentsQuery> {

        private final EmailValidator emailValidator;
        private final PositiveValidator positiveValidator;
        private final MessageLocalizer messageLocalizer;

        @Inject
        public FindAllManagersQueryValidator(Logger logger, DateRangeValidator dateValidator, EmailValidator emailValidator, PositiveValidator positiveValidator, MessageLocalizer messageLocalizer) {
            super(logger, dateValidator);
            this.emailValidator = emailValidator;
            this.positiveValidator = positiveValidator;
            this.messageLocalizer = messageLocalizer;
        }

        @Override
        protected void validateExtra(FindAllStudentsQuery value) {
            value.createdBy().ifPresent(
                    email -> validate(() -> emailValidator.validate(Fields.CREATED_BY.getValue(), email))
            );
            value.academicQuarter().ifPresent(
                    aq -> validate(() -> positiveValidator.validate(Fields.ACADEMIC_QUARTER.getValue(), aq))
            );
            value.academicGroup().ifPresent(
                    ag -> validate(() -> {
                        if(!QuarterGroup.isValid(ag)){
                            throw new FormatViolation(messageLocalizer, Fields.ACADEMIC_GROUP.getValue(), String.valueOf(ag));
                        }
                    })
            );
        }
    }
}
