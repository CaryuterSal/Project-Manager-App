package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface AnyUserRepository{
    Optional<? extends User<?>> findById(User.Id id);
    List<? extends User<?>> findAll();
    boolean existsById(User.Id id);
    void deleteById(User.Id id);
    void delete(User<?> user);

}
