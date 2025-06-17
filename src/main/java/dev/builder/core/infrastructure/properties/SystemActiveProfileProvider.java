package dev.builder.core.infrastructure.properties;

import java.util.Optional;

public class SystemActiveProfileProvider implements ActiveProfileProvider {

    static final String profilePropertyNamespace = "profile";
    private final String activeProfile;

    public SystemActiveProfileProvider() {
        activeProfile = System.getProperty(profilePropertyNamespace);
    }

    @Override
    public Optional<String> getActiveProfile() {
        return Optional.ofNullable(activeProfile);
    }
}
