package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Stage;
import dev.builder.board.domain.model.Task;
import dev.builder.core.domain.port.CrudRepository;
import dev.builder.core.domain.port.TransactionalCrudRepository;

import java.sql.Connection;
import java.util.Optional;
import java.util.Set;

public interface StageRepository extends TransactionalCrudRepository<Stage, Stage.Id> {
    Set<Stage> findByBoard(Board.Id board);
    Set<Stage> findByBoard(Board.Id board, Connection connection);
    boolean existsByBoard(Board.Id stage);
    boolean existsByBoard(Board.Id board, Connection connection);
    Optional<Stage> findByBoardAndContainingTask(Board.Id boardId,Task.Id taskId);
    Optional<Stage> findByBoardAndContainingTask(Board.Id boardId, Task.Id taskId, Connection connection);
    Optional<Stage> findByContainingTask(Task.Id taskId);
    Optional<Stage> findByContainingTask(Task.Id taskId, Connection connection);
}
