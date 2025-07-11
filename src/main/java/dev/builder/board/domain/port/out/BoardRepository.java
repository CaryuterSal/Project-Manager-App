package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.model.Stage;
import dev.builder.core.domain.CrudRepository;

import java.util.Set;

public interface BoardRepository extends CrudRepository<Board, Board.Id> {
}
