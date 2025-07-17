package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Student;

public interface StudentRepository extends UserRepository<Student, Student.Id> {
}
