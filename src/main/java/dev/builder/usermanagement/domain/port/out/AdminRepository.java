package dev.builder.usermanagement.domain.port.out;

import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface AdminRepository extends UserRepository<Admin, Admin.Id>{
    Optional<Admin> findByCreatedManager(Manager.Id id);
    Optional<Admin> findByCreatedManager(Manager.Id id, Connection connection);
}
