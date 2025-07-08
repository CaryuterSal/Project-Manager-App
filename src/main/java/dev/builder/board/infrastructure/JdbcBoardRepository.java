package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Board;
import dev.builder.board.domain.port.out.BoardRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.List;
import java.util.Optional;

@Bean
public class JdbcBoardRepository implements BoardRepository {

    @Override
    public Optional<Board> findById(Board.Id id) {
        return Optional.empty();
    }

    @Override
    public List<Board> findAll() {
        return List.of();
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
    public boolean existsById(Board.Id id) {
        return false;
    }

    @Override
    public Board update(Board aggregateRoot) {
        return null;
    }
}
