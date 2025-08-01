package dev.builder.usermanagement.domain.port.out;

import dev.builder.core.domain.port.TransactionalCrudRepository;
import dev.builder.usermanagement.domain.model.User;

import java.sql.Connection;

public interface UserRepository<E extends User<ID>, ID extends User.Id> extends TransactionalCrudRepository<E, ID> {
    boolean existsDeletedById(ID id);
    boolean existsDeletedById(ID id, Connection connection);
}
