package dev.builder.usermanagement;

import dev.builder.usermanagement.domain.model.Password;
import dev.builder.usermanagement.domain.port.out.PasswordHasher;
import dev.builder.usermanagement.infrastructure.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BcryptPasswordHasherTest {

    private final PasswordHasher passwordHasher = new BCryptPasswordHasher();

    @Test
    void test_encode_password(){
        Password password = new Password("newPassword123#");
        String hashedPassword = passwordHasher.encode(password);
        assertThat(password.value()).isNotEqualTo(hashedPassword);
        assertTrue(passwordHasher.matches(password, hashedPassword));
    }
}
