package dev.builder.usermanagement.domain.port.out;

import dev.builder.core.domain.port.TransactionalCrudRepository;
import dev.builder.usermanagement.domain.model.User;

public interface UserRepository<E extends User<ID>, ID extends User.Id> extends TransactionalCrudRepository<E, ID> {
}
