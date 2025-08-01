package dev.builder.auth.infrastructure;

import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.domain.port.out.SessionStorage;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.SessionFileProperties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Bean
public class FileSessionStorage implements SessionStorage {

    private final SessionContext sessionContext;
    private final TokenPersister fileAccess;
    private final String filename;
    private final Duration ttl;

    @Inject
    public FileSessionStorage(SessionContext sessionContext, TokenPersister fileAccess, SessionFileProperties filenameProvider) {
        this.sessionContext = sessionContext;
        this.fileAccess = fileAccess;
        this.filename = filenameProvider.getFilename();
        this.ttl = filenameProvider.getTTL();
    }

    @Override
    public Optional<SessionToken> restoreSession() {
        try {
            SessionToken token = fileAccess.read(filename);
            if(Instant.now().isAfter(token.expiration())) {
                return Optional.empty();
            } else {
                return Optional.of(token);
            }
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public void saveSession() {
        try {
            fileAccess.save(generateSessionToken(), filename);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearSession() {
        try{
            Files.deleteIfExists(Paths.get(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(" -> new")
    private @NotNull SessionToken generateSessionToken() {
        return new SessionToken(
                sessionContext.getCurrentUser(),
                sessionContext.getCurrentRole(),
                Instant.now().plus(ttl)
        );
    }
}
