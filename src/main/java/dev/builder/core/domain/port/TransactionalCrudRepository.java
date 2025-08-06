package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface TransactionalCrudRepository<T extends Entity<ID>, ID> extends
        TransactionalCreateRepository<T, ID>,
        TransactionalDeleteRepository<T, ID>,
        TransactionalReadRepository<T,ID>{

}
