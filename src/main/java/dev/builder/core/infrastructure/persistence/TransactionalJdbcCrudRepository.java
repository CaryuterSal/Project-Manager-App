package dev.builder.core.infrastructure.persistence;


import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.*;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;
import dev.builder.core.domain.port.TransactionalCrudRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class TransactionalJdbcCrudRepository<T extends Entity<ID>, ID> implements TransactionalCrudRepository<T, ID> {

    protected abstract Logger getLogger();

    protected final ConnectionManager connectionManager;

    protected TransactionalJdbcCrudRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<T> findById(ID id) {
        return wrapWithConnection(connectionManager, getLogger(), this::findById, id);
    }

    @Override
    public List<T> findAll() {
        return wrapWithConnection(connectionManager, getLogger(), this::findAll);
    }

    @Override
    public boolean delete(T entity) {
        return deleteById(entity.id());
    }

    @Override
    public boolean delete(T entity, Connection connection) {
        return deleteById(entity.id(), connection);
    }

    @Override
    public T save(T entity) {
        return wrapWithConnection(connectionManager, getLogger(), this::save, entity);
    }

    @Override
    public boolean existsById(ID id) {
        return wrapWithConnection(connectionManager, getLogger(), this::existsById, id);
    }

    @Override
    public boolean deleteById(ID id) {
        return wrapWithConnection(connectionManager, getLogger(),this::deleteById, id);
    }

    protected boolean existsById(String existsQuery, PreparedStatementFiller paramFiller, ID id, Connection connection){
        return executeQuery(
                existsQuery,
                paramFiller,
                rs -> rs.next() && rs.getInt("total") > 0,
                getLogger(),
                connection
        );
    }
}
