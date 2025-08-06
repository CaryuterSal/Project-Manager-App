package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Board;
import dev.builder.core.domain.port.CrudRepository;
import dev.builder.core.domain.port.TransactionalCrudRepository;

public interface BoardRepository extends TransactionalCrudRepository<Board, Board.Id> {
}
