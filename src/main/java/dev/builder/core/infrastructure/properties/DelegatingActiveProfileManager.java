package dev.builder.core.infrastructure.properties;

import dev.builder.core.infrastructure.di.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DelegatingActiveProfileManager implements ActiveProfileManager{

    private final Map<String, ActiveProfileProvider> providers;
    private final ActiveProfileProvider preferredProvider;

    public DelegatingActiveProfileManager(String preferred, Map<String, ActiveProfileProvider> providers) {
        if(preferred == null) throw new IllegalArgumentException("preferred provider must be specified");
        if(providers == null || providers.isEmpty()) throw new IllegalArgumentException("at least one provider must be provided");
        this.providers = providers;
        this.preferredProvider = providers.get(preferred);
        if(preferredProvider == null) throw new IllegalArgumentException("preferred provider could not be found");
        this.providers.remove(preferred);
    }

    @Override
    public Optional<String> getActiveProfile() {
        Optional<String> profile = preferredProvider.getActiveProfile();
        if(profile.isEmpty()){
            for(ActiveProfileProvider provider: providers.values()){
                Optional<String> fallback = provider.getActiveProfile();
                if(fallback.isPresent()) return fallback;
            }
        }
        return Optional.empty();
    }
}
