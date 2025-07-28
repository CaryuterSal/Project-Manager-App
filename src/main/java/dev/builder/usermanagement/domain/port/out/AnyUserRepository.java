package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.User;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface AnyUserRepository{
    Optional<? extends User<?>> findById(User.Id id);
    Optional<? extends User<?>> findById(User.Id id, Connection connection);
    List<? extends User<?>> findAll();
    List<? extends User<?>> findAll(Connection connection);
    boolean existsById(User.Id id);
    boolean existsById(User.Id id, Connection connection);
    boolean deleteById(User.Id id);
    boolean deleteById(User.Id id, Connection connection);
    boolean delete(User<?> user);
    boolean delete(User<?> user, Connection connection);

    boolean existsDeletedById(User.Id id);
    boolean existsDeletedById(User.Id id, Connection connection);

}
