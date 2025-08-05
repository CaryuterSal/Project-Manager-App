package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Password;

/**
 * Comprueba si una contraseña es la misma que la encriptada
 */
@FunctionalInterface
public interface PasswordMatcher {
    boolean matches(Password plainPassword, String encodedPassword);
}
