package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Password;

public interface PasswordEncoder {
    String encode(Password plainPassword);
    boolean matches(Password plainPassword, String encodedPassword);
}
