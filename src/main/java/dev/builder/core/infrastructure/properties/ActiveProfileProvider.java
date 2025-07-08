package dev.builder.core.infrastructure.properties;

import java.util.Optional;

public interface ActiveProfileProvider{
    Optional<String> getActiveProfile();
}
