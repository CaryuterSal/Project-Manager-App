package dev.builder.usermanagement.domain.model;

import java.util.UUID;

public class User {

    private final UserId id;

    public User(UserId id) {
        this.id = id;
    }


    public record UserId(UUID id) {}
}
