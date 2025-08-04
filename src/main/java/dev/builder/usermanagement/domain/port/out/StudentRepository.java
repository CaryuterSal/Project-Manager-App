package dev.builder.usermanagement.domain.port.out;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Task;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

public interface StudentRepository extends UserRepository<Student, Student.Id> {
    List<Student> findByCreatedBy(Manager.Id id);
    List<Student> findByCreatedBy(Manager.Id id, Connection connection);
    Set<Student> findAssignedToTask(Task.Id id);
    Set<Student> findAssignedToTask(Task.Id id, Connection connection);
    Set<Student> findCollaboratingOnBoard(Board.Id id);
    Set<Student> findCollaboratingOnBoard(Board.Id id, Connection connection);
}
