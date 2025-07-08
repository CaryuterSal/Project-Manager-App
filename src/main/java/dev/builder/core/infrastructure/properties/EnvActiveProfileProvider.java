package dev.builder.core.infrastructure.properties;

import java.util.Optional;

public class EnvActiveProfileProvider implements ActiveProfileProvider {

    private final String activeProfile;

    public EnvActiveProfileProvider() {
        activeProfile = System.getenv(PropertiesNamespaces.ENV_PROFILE_NAMESPACE);
    }

    @Override
    public Optional<String> getActiveProfile() {
        return Optional.ofNullable(activeProfile);
    }
}
