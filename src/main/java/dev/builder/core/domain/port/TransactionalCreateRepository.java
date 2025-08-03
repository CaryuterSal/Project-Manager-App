package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;
import org.apache.poi.ss.formula.functions.T;

import java.sql.Connection;

public interface TransactionalCreateRepository<T extends Entity<ID>, ID> extends CreateRepository<T, ID>{

    T save(T aggregateRoot, Connection connection);
}
