package dev.builder.core.infrastructure.properties;

import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.Optional;

@Bean
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
