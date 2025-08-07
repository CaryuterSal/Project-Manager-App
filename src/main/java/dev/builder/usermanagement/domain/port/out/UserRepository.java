package dev.builder.usermanagement.domain.port.out;

import dev.builder.core.domain.port.TransactionalCrudRepository;
import dev.builder.usermanagement.domain.model.User;

import java.sql.Connection;
import java.util.List;

public interface UserRepository<E extends User<ID>, ID extends User.Id<ID>> extends TransactionalCrudRepository<E, ID> {
    boolean existsDeletedById(ID id);
    boolean existsDeletedById(ID id, Connection connection);
    List<E> findWithEmailLike(String emailLike);
    List<E> findWithEmailLike(String emailLike, Connection connection);
}
