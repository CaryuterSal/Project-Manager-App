package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;

public interface DeleteRepository<T extends Entity<ID>, ID> extends Repository<T, ID> {

    boolean delete(T aggregateRoot);
    boolean deleteById(ID id);
}
