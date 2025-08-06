package dev.builder.jdbc;

import dev.builder.shared.ContainerizedTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class ContainerizedConnectionTest extends ContainerizedTest {

    @Override
    protected void setup() {
    }

    @Override
    protected void setupFirstRun() {

    }

    @Test
    void testGetConnection() throws SQLException {
        connectionManager.getConnection();
    }

}
