package dev.builder.auth.infrastructure;

import java.io.Serializable;
import java.time.Instant;

public record SessionToken(String email, Role role, Instant expiration) implements Serializable {
}
