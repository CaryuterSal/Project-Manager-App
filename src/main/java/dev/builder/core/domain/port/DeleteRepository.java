package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;

public interface DeleteRepository<T extends AggregateRoot<ID>, ID> extends Repository<T, ID> {

    boolean delete(T aggregateRoot);
    boolean deleteById(ID id);
}
