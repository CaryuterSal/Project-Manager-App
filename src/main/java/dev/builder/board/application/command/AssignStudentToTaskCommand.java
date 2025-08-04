package dev.builder.board.application.command;

import dev.builder.core.application.Command;
import dev.builder.core.application.validation.BaseRequestValidator;
import dev.builder.core.application.validation.RequiredObjectValidator;
import dev.builder.core.application.validation.ValidationException;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public record AssignStudentToTaskCommand(UUID taskId, String studentEmail) implements Command<Void> {
    public enum Fields{
        TASK_ID("taskId"), EMAIL("email");
        private final String value;
        Fields(String value) {this.value = value;}
        public String value() {return value;}
    }

    @Bean
    public static class AssignStudentToTaskCommandValidator extends BaseRequestValidator<AssignStudentToTaskCommand>{
        private static final Logger log = LoggerFactory.getLogger(AssignStudentToTaskCommandValidator.class);
        private final RequiredObjectValidator requiredObjectValidator;
        private final EmailValidator emailValidator;

        @Inject
        public AssignStudentToTaskCommandValidator( RequiredObjectValidator requiredObjectValidator, EmailValidator emailValidator) {
            super(log);
            this.requiredObjectValidator = requiredObjectValidator;
            this.emailValidator = emailValidator;
        }

        @Override
        public void validate(AssignStudentToTaskCommand value) throws ValidationException {
            validate(() -> requiredObjectValidator.validate(Fields.TASK_ID.value, value.taskId));
            validate(() -> emailValidator.validate(Fields.EMAIL.value, value.studentEmail));
            throwIfAny();
        }
    }
}
