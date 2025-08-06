package dev.builder.core.infrastructure.persistence;

import java.sql.Connection;

/**
 * Representa una operación de repositorio con dos parámetros de entrada apta para transacciones (acepta una conexión existente)
 * @param <T> el tipo del primer parámetro de entrada
 * @param <Y> el tipo del segundo parámetro de entrada
 * @param <V> el tipo de resultado de salida
 */
@FunctionalInterface
public interface TransactionalBiOperation<T,Y,V> {
    V execute(T inOne, Y inTwo, Connection connection);
}
