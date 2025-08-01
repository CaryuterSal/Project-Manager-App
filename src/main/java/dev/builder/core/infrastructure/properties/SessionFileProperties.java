package dev.builder.core.infrastructure.properties;

import java.time.Duration;

/**
 * Obtiene el nombre del archivo en el que se persiste la sesión del usuario
 */
public interface SessionFileProperties {
    /**
     * Obtiene el nombre del archivo en el que se persiste la sesión del usuario
     * @return nombre del archivo
     */
    String getFilename();

    /**
     * Obtiene el tiempo de validez de la sesión del usuario
     * @return la duración de sesión
     */
    Duration getTTL();
}
