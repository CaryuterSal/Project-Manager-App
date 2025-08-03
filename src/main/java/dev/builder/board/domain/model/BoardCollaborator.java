package dev.builder.board.domain.model;

import dev.builder.core.domain.LocalEntity;
import dev.builder.usermanagement.domain.model.Student;

import java.time.LocalDateTime;
import java.util.Objects;

public class BoardCollaborator extends LocalEntity<Student.Id> {

    private LocalDateTime issuedAt;

    public BoardCollaborator(LocalDateTime issuedAt, Student.Id student) {
        super(student);
        validate(issuedAt, student);
        this.issuedAt = issuedAt;
    }

    public BoardCollaborator(Student.Id student){
        this(LocalDateTime.now(), student);
    }

    public void hydrateWithAuditInfo(LocalDateTime issuedAt){
        this.issuedAt = issuedAt;
    }

    public LocalDateTime issuedAt() {
        return issuedAt;
    }

    public static void validate(LocalDateTime issuedAt, Student.Id student) {
        if(!isValid(Objects.requireNonNull(issuedAt), Objects.requireNonNull(student))) {
            throw new IllegalArgumentException("Task assignation is invalid");
        }
    }
    public static boolean isValid(LocalDateTime issuedAt, Student.Id student) {
        return issuedAt != null && student != null &&(issuedAt.equals(LocalDateTime.now()) || issuedAt.isBefore(LocalDateTime.now()));
    }
}
