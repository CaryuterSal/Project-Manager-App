package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import org.apache.poi.ss.formula.functions.T;

import java.sql.Connection;

public interface TransactionalCreateRepository<T extends AggregateRoot<ID>, ID> extends CreateRepository<T, ID>{

    T save(T aggregateRoot, Connection connection);
}
