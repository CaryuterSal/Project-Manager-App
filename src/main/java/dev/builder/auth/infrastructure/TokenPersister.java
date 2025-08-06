package dev.builder.auth.infrastructure;

import java.io.FileNotFoundException;

public interface TokenPersister {
    void save(SessionToken content, String filename) throws FileNotFoundException;
    SessionToken read(String filename) throws FileNotFoundException;
}
