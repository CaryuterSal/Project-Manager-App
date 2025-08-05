package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Password;

/**
 * Combina las tareas de codificar contraseñas y comprobar la igualdad entre una no codificada y una codificada
 */
public interface PasswordHasher extends PasswordMatcher, PasswordEncoder {
}
