package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface TransactionalReadRepository<T extends AggregateRoot<ID>, ID> extends ReadRepository<T, ID> {

    List<T> findAll(Connection connection);
    Optional<T> findById(ID id, Connection connection);
    boolean existsById(ID id, Connection connection);
}
