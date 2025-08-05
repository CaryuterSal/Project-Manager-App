package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T extends AggregateRoot<ID>, ID> extends
        ReadRepository<T, ID>,
        DeleteRepository<T,ID>,
        CreateRepository<T, ID> {
}
