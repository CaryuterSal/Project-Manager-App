package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.port.out.BoardRepository;

import java.util.Optional;

public class JdbcBoardRepository implements BoardRepository {
    @Override
    public Optional<Board> findById(Board.Id id) {
        return Optional.empty();
    }

    @Override
    public void delete(Board aggregateRoot) {

    }

    @Override
    public void deleteById(Board.Id id) {

    }

    @Override
    public Board save(Board aggregateRoot) {
        return null;
    }

    @Override
    public Board update(Board aggregateRoot) {
        return null;
    }
}
