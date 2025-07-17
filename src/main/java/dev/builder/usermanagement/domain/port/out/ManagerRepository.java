package dev.builder.usermanagement.domain.port.out;

import dev.builder.core.domain.CrudRepository;

import dev.builder.usermanagement.domain.model.Manager;

public interface ManagerRepository extends UserRepository<Manager, Manager.Id> {
}
