package dev.builder.core.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Convierte un resultado de {@link ResultSet} en la entidad deseada
 * @param <T> El tipo de entidad
 */
@FunctionalInterface
public interface RowMapper<T> {

    T map(ResultSet rs) throws SQLException;
}
