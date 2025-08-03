package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;

import java.sql.Connection;

public interface TransactionalDeleteRepository<T extends Entity<ID>, ID> extends DeleteRepository<T, ID> {

    boolean delete(T aggregateRoot, Connection connection);
    boolean deleteById(ID id, Connection connection);
}
