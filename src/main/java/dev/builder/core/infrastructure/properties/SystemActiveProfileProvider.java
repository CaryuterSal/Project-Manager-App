package dev.builder.core.infrastructure.properties;

import java.util.Optional;

public class SystemActiveProfileProvider implements ActiveProfileProvider {

    private final String activeProfile;

    public SystemActiveProfileProvider() {
        activeProfile = System.getProperty(PropertiesNamespaces.PROPERTIES_PROFILE_NAMESPACE);
    }

    @Override
    public Optional<String> getActiveProfile() {
        return Optional.ofNullable(activeProfile);
    }
}
