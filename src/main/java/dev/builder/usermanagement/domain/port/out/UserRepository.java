package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.User;

public interface UserRepository {

    User findById(User.UserId id);

    User save(User user);

    User update(User user);

    void deleteById(User.UserId id);

    void delete(User user);


}
