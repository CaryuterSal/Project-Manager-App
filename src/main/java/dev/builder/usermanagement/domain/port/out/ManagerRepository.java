package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends UserRepository<Manager, Manager.Id> {
    List<Manager> findByCreatedBy(Admin.Id id);
    List<Manager> findByCreatedBy(Admin.Id id, Connection connection);
    Optional<Manager> findByCreatedStudent(Student .Id id);
    Optional<Manager> findByCreatedStudent(Student .Id id, Connection connection);
}
