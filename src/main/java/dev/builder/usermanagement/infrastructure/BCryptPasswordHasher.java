package dev.builder.usermanagement.infrastructure;

import com.password4j.BcryptFunction;
import com.password4j.Hash;
import com.password4j.types.Bcrypt;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.usermanagement.domain.model.Password;
import dev.builder.usermanagement.domain.port.out.PasswordHasher;

@Bean
public class BCryptPasswordHasher implements PasswordHasher {

    private static final BcryptFunction bcrypt = BcryptFunction.getInstance(Bcrypt.B, 12);

    @Override
    public String encode(Password plainPassword) {
        Hash hash = com.password4j.Password.hash(plainPassword.value())
                .with(bcrypt);

        return hash.getResult();
    }

    @Override
    public boolean matches(Password plainPassword, String encodedPassword) {
        return com.password4j.Password.check(plainPassword.value(),encodedPassword)
                .with(bcrypt);

    }
}
