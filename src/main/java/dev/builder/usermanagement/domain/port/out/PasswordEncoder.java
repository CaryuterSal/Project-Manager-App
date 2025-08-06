package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Password;

/**
 * Codifica una contraseña
 */
@FunctionalInterface
public interface PasswordEncoder {
    String encode(Password plainPassword);
}
