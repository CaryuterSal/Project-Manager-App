package dev.builder.core.infrastructure.persistence;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonMappers {

    /**
     * Itera sobre un {@link ResultSet} de JDBC y agrupa las filas en un {@link Map} donde cada clave
     * se asigna a un {@link Set} de valores asociados.
     *
     * <p>La asignación se controla mediante dos funciones de extracción: una para la clave y otra para el valor.
     * Este método consume todas las filas del conjunto de resultados y luego lo restablece a la primera fila.
     *
     * Requiere que el {@link ResultSet} sea al menos {@code TYPE_SCROLL_INSENSITIVE}
     *
     * @param rs el JDBC ResultSet, ya ubicado en la primera fila.
     * @param keyExtractor: función para extraer la clave de cada fila.
     * @param valueExtractor: función para extraer el valor de cada fila.
     * @return un mapa donde cada clave está asociada a un conjunto de valores.
     * @throws SQLException: si se produce un error de acceso a la base de datos.
     * @param <K> tipo de clave.
     * @param <V> tipo de valor.
     */

    public static <K, V> @NotNull Map<K, Set<V>> groupResultSetByKey(
            @NotNull ResultSet rs,
            @NotNull SQLFunction<ResultSet, K> keyExtractor,
            @NotNull SQLFunction<ResultSet, V> valueExtractor
    ) throws SQLException {
        Map<K, Set<V>> result = new HashMap<>();
        do {
            K creator = keyExtractor.apply(rs);
            V created = valueExtractor.apply(rs);
            if(created != null){
                result.computeIfAbsent(creator, k -> new HashSet<>()).add(created);
            }
        } while (rs.next());
        rs.first();
        return result;
    }
}
