package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;

import java.sql.Connection;
import java.util.List;

public interface StudentRepository extends UserRepository<Student, Student.Id> {
    List<Student> findByCreatedBy(Manager.Id id);
    List<Student> findByCreatedBy(Manager.Id id, Connection connection);
}
