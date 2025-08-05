package dev.builder.auth.domain.port.out;

import dev.builder.auth.infrastructure.SessionToken;

import java.util.Optional;

/**
 * Interfaz para definir operaciones de persistencia de sesión en dispositivos cliente.
 *
 * Esta interfaz abstrae la lógica para guardar, restaurar y eliminar la sesión actual
 * entre ejecuciones de la aplicación.
 *
 * La fuente de verdad de la sesión activa es el singleton {@link SessionContext}, por lo tanto,
 * las implementaciones deben interactuar directamente con él para obtener o establecer el estado.
 *
 * Las implementaciones pueden utilizar archivos locales, almacenamiento en base de datos embebida,
 * o cualquier otro mecanismo persistente.
 */
public interface SessionStorage {
    /**
     * Intenta restaurar la sesión desde el medio persistente.
     *
     * @return El tóken con la información para restaurar la sesión
     */
    Optional<SessionToken> restoreSession();
    /**
     * Persiste el estado actual de sesión, obtenido desde {@link SessionContext}.
     *
     * Este método debe llamarse después de una autenticación exitosa si se desea
     * mantener la sesión entre reinicios de la aplicación.
     */
    void saveSession();
    /**
     * Elimina cualquier información persistente de sesión, por ejemplo al cerrar sesión.
     *
     * También debe invocar a {@link SessionContext#clear()} para eliminar la sesión en memoria.
     */
    void clearSession();
}
