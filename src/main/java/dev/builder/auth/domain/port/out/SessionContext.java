package dev.builder.auth.domain.port.out;

import dev.builder.auth.application.service.UnauthorizedException;
import dev.builder.auth.infrastructure.Role;
import dev.builder.usermanagement.domain.model.User;
import org.jetbrains.annotations.NotNull;

public interface SessionContext {

    /**
     * Establece el contexto de autenticación para el usuario indicado.
     *
     * @param user El usuario que ha iniciado sesión. No debe ser {@code null}.
     */
    void setAuthentication(@NotNull User<?> user);

    /**
     * Retorna el correo electrónico del usuario actualmente autenticado.
     *
     * @return el correo electrónico como {@code String}, o {@code null} si no hay sesión activa.
     */
    String getCurrentUser();

    /**
     * Verifica que haya un usuario con sesión actualmente
     * @return verdadero si hay una sesión activa
     */
    boolean isAuthenticated();

    /**
     * Obtiene el rol actual del usuario
     * @return el rol del usuario, o {@code null} si no hay ninguno autenticado
     */
    Role getCurrentRole();
    /**
     * Verifica si el usuario autenticado posee el rol indicado.
     *
     * @param role El rol a verificar. No debe ser {@code null}.
     * @return {@code true} si el rol coincide; {@code false} en caso contrario.
     */
    boolean hasRole(@NotNull Role role);

    /**
     * Verifica si el usuario autenticado posee el rol indicado. falla si no lo tiene
     *
     * @param role El rol a verificar. No debe ser {@code null}.
     * @param message El mensaje a lanzar con la excepción
     * @throws UnauthorizedException si no tiene el rol indicado
     */
    default void requireRole(@NotNull Role role, String message) throws UnauthorizedException{
        if(!hasRole(role)){
            throw new UnauthorizedException(message);
        }
    }

    /**
     * Verifica si el usuario autenticado posee alguno de los roles indicados.
     *
     * @param roles Los roles a verificar. No deben ser {@code null}.
     * @return {@code true} si alguno de los roles coincide, {@code false} en caso contrario.
     */
    default boolean hasAnyRole(@NotNull Role @NotNull ... roles) {
        for (Role role : roles) {
            if (hasRole(role)) return true;
        }
        return false;
    }

    /**
     * Limpia el contexto de sesión actual.
     * Esta operación se usa comúnmente al cerrar sesión.
     */
    void clear();
}
