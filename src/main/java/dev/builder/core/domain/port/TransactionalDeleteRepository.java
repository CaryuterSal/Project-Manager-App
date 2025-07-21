package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;

import java.sql.Connection;

public interface TransactionalDeleteRepository<T extends AggregateRoot<ID>, ID> extends DeleteRepository<T, ID> {

    boolean delete(T aggregateRoot, Connection connection);
    boolean deleteById(ID id, Connection connection);
}
