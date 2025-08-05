package dev.builder.core.infrastructure.properties;

import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Bean
public class ActiveProfileConfiguration implements ActiveProfileManager {

    private final ActiveProfileManager proxyManager;

    public ActiveProfileConfiguration(EnvActiveProfileProvider envProfileProvider, SystemActiveProfileProvider systemProfileProvider) {
        Map<String, ActiveProfileProvider> activeProfileProviders = new HashMap<>();
        activeProfileProviders.put("env", envProfileProvider);
        activeProfileProviders.put("system", systemProfileProvider);
        proxyManager = new DelegatingActiveProfileManager("system", activeProfileProviders);
    }
    @Override
    public Optional<String> getActiveProfile() {
        return proxyManager.getActiveProfile();
    }
}
