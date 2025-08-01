package dev.builder.core.infrastructure.persistence;
import java.sql.SQLException;

/**
 * Similar a un {@link java.util.function.Consumer} pero para operaciones relacionadas con JDBC
 */
@FunctionalInterface
public interface SQLConsumer<T> {
    void accept(T in) throws SQLException;
}
