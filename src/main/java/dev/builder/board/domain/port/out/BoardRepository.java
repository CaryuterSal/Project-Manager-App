package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Board;
import dev.builder.core.domain.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends CrudRepository<Board, Board.Id> {

    /**
     * encuentra el tablero con el id
     * @param id el id del tablero
     * @return el tablero si existe, o un optional vacio
     * @throws IllegalArgumentException cuando el id es {@code null}
     */
    Optional<Board> findById(Board.Id id);
    List<Board> findAll();
    void deleteById(Board.Id id);
    void delete(Board board);
    Board save(Board board);
    boolean existsById(Board.Id id);
}
