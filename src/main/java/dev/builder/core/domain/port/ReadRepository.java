package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;

import java.util.List;
import java.util.Optional;

public interface ReadRepository<T extends Entity<ID>, ID> extends Repository<T,ID>{
    List<T> findAll();
    Optional<T> findById(ID id);
    boolean existsById(ID id);
}
