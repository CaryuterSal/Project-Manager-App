package dev.builder.usermanagement.domain.exception;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.model.Student;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(MessageLocalizer localizer, Student.Id studentId) {
        super(localizer.getMessage("user.student.not.found", studentId.value()));
    }
}
