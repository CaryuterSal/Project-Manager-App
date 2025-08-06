package dev.builder.core.infrastructure.properties;

import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.Optional;

@Bean
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
