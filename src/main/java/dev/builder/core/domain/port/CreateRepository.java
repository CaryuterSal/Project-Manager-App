package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;

public interface CreateRepository<T extends Entity<ID>, ID> extends Repository<T, ID> {
    T save(T aggregateRoot);
}
