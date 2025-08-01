package dev.builder.core.infrastructure.persistence;

import java.sql.Connection;

/**
 * Representa una operación de repositorio con un parámetro de entrada apta para transacciones (acepta una conexión existente)
 * @param <T> El tipo del parámetro de entrada
 * @param <V> el tipo de resultado de salida
 */
@FunctionalInterface
public interface TransactionalOperation<T, V> {

    V execute(T inputParam, Connection connection);
}
