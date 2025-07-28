package dev.builder.jdbc;

import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.core.infrastructure.persistence.DefaultConnectionManager;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class OracleProdConnectionTest {

    private final ConnectionManager connectionManager = new DefaultConnectionManager();
    @Test
    void testGetConnection() throws SQLException {
        connectionManager.getConnection();
    }
}
