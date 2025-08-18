package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Board;
import dev.builder.core.domain.port.CrudRepository;
import dev.builder.core.domain.port.TransactionalCrudRepository;
import dev.builder.usermanagement.domain.model.Student;

import java.sql.Connection;
import java.util.List;

public interface BoardRepository extends TransactionalCrudRepository<Board, Board.Id> {
    List<Board> findAllByCollaborator(Student.Id  studentId);
    List<Board> findAllByCollaborator(Student.Id studentId, Connection connection);
}
