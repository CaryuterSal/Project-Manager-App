package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Board;
import dev.builder.core.domain.port.CrudRepository;

public interface BoardRepository extends CrudRepository<Board, Board.Id> {
}
