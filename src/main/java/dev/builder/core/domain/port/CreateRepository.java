package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;

public interface CreateRepository<T extends AggregateRoot<ID>, ID> {
    T save(T aggregateRoot);
}
