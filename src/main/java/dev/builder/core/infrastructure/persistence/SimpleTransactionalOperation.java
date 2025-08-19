package dev.builder.core.infrastructure.persistence;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * Representa una operación de repositorio sin parámetros de entrada apta para transacciones (acepta una conexión existente)
 * @param <V> el tipo de resultado de salida
 */
@FunctionalInterface
public interface SimpleTransactionalOperation<V> {
    V execute(Connection connection) throws Exception;
}