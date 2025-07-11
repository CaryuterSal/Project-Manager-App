package dev.builder.core.domain;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T extends AggregateRoot<ID>, ID> extends Repository<T, ID>{

    Optional<T> findById(ID id);

    List<T> findAll();

    void delete(T aggregateRoot);

    void deleteById(ID id);

    T save(T aggregateRoot);

    boolean existsById(ID id);
}
