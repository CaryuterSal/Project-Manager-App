package dev.builder.jdbc;

import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.core.infrastructure.persistence.DefaultConnectionManager;
import dev.builder.core.infrastructure.properties.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class OracleProdConnectionTest {

    private static ConnectionManager connectionManager;

    @BeforeAll
    static void setUp() throws SQLException {
        EnvActiveProfileProvider envActiveProfileProvider = new EnvActiveProfileProvider();
        SystemActiveProfileProvider systemActiveProfileProvider = new SystemActiveProfileProvider();
        ActiveProfileConfiguration activeProfileConfiguration = new ActiveProfileConfiguration(envActiveProfileProvider, systemActiveProfileProvider);
        connectionManager = new DefaultConnectionManager(new SimpleApplicationProperties(activeProfileConfiguration));
    }

    @Test
    void testGetConnection() throws SQLException {
        connectionManager.getConnection();
    }
}
