package dev.builder.usermanagement.infrastructure;

import dev.builder.usermanagement.domain.model.Password;
import dev.builder.usermanagement.domain.port.out.PasswordEncoder;

public class BCryptPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(Password plainPassword) {
        return "";
    }

    @Override
    public boolean matches(Password plainPassword, String encodedPassword) {
        return false;
    }
}
