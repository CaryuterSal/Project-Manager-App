package dev.builder.usermanagement.domain.port.out;

import dev.builder.core.domain.CrudRepository;
import dev.builder.usermanagement.domain.model.User;
import org.glassfish.jaxb.core.v2.model.core.ID;

public interface UserRepository<E extends User<ID>, ID extends User.Id> extends CrudRepository<E, ID> {
}
