package dev.builder.core.infrastructure.persistence;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommonJdbcOperationWrappers {

    public static <T> T executeQuery(String query, @NotNull PreparedStatementFiller filler, @NotNull RowMapper<T> resultExtractor, Logger logger, Connection connection){
        try(PreparedStatement ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){
            filler.accept(ps);
            try(ResultSet rs = ps.executeQuery()) {
                return resultExtractor.map(rs);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }


    public static <IN, V> V wrapWithConnection(ConnectionManager connectionManager, Logger logger,@NotNull TransactionalOperation<IN, V> operation, IN inParam){
        try(Connection conn = connectionManager.getConnection()){
            return operation.execute(inParam, conn);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public static <V> V wrapWithConnection(ConnectionManager connectionManager, Logger logger, @NotNull SimpleTransactionalOperation<V> operation){
        try(Connection conn = connectionManager.getConnection()){
            return operation.execute(conn);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public static <V> V runInTransaction(ConnectionManager connectionManager, Logger logger, SimpleTransactionalOperation<V> operation) {
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            connection.setAutoCommit(false);

            V result = operation.execute(connection);
            connection.commit();
            return result;

        } catch (SQLException ex) {
            rollBackIfNeeded(connection, logger);
            throw new RepositoryException(ex.getMessage(), ex);
        } catch (RepositoryException ex){
            rollBackIfNeeded(connection, logger);
            throw ex;
        } finally {
           closeConnectionIfNeeded(connection, logger);
        }
    }


    public static <IN,V> V runInTransaction(ConnectionManager connectionManager, Logger logger, TransactionalOperation<IN,V> operation, IN inParam) {
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            connection.setAutoCommit(false);

            V result = operation.execute(inParam, connection);
            connection.commit();
            return result;

        } catch (SQLException ex) {
           rollBackIfNeeded(connection, logger);
           throw new RepositoryException(ex.getMessage(), ex);
        } catch (RepositoryException ex){
            rollBackIfNeeded(connection, logger);
            throw ex;
        } finally {
            closeConnectionIfNeeded(connection, logger);
        }
    }

    public static void rollBackIfNeeded(Connection connection, Logger logger) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Rollback failed: " + rollbackEx.getMessage(), rollbackEx);
            }
        }
    }

    public static void closeConnectionIfNeeded(Connection connection, Logger logger) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException closeEx) {
                logger.error("Closing connection failed: " + closeEx.getMessage(), closeEx);
            }
        }
    }

    public static void runOracleScript(ConnectionManager connectionManager, String pathInClasspath) {
        try (Connection connection = connectionManager.getConnection()) {
            try (InputStream input = CommonJdbcOperationWrappers.class.getResourceAsStream(pathInClasspath)) {
                if (input == null) {
                    throw new IllegalArgumentException("SQL script not found: " + pathInClasspath);
                }

                List<String> statements = new ArrayList<>();
                StringBuilder current = new StringBuilder();
                boolean insidePlsqlBlock = false;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim();

                        // Skip empty lines and full-line comments
                        if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;

                        // Detect end of PL/SQL block
                        if (trimmed.equals("/") && insidePlsqlBlock) {
                            statements.add(current.toString());
                            current.setLength(0);
                            insidePlsqlBlock = false;
                            continue;
                        }

                        current.append(line).append("\n");

                        // Detect end of SQL statement
                        if (!insidePlsqlBlock && trimmed.endsWith(";")) {
                            statements.add(current.toString().trim());
                            current.setLength(0);
                        }

                        // Detect beginning of a PL/SQL block
                        if (!insidePlsqlBlock && (
                                trimmed.toUpperCase().startsWith("CREATE OR REPLACE FUNCTION") ||
                                trimmed.toUpperCase().startsWith("CREATE OR REPLACE PROCEDURE") ||
                                trimmed.toUpperCase().startsWith("CREATE OR REPLACE PACKAGE") ||
                                trimmed.toUpperCase().startsWith("CREATE OR REPLACE TRIGGER") ||
                                trimmed.toUpperCase().startsWith("BEGIN")
                        )) {
                            insidePlsqlBlock = true;
                        }
                    }

                    // Catch anything left
                    if (!current.isEmpty()) {
                        statements.add(current.toString().trim());
                    }
                }

                try (Statement stmt = connection.createStatement()) {
                    for (String sql : statements) {
                        String trimmed = sql.trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed.replaceAll(";$", "")); // remove trailing ;
                        }
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to run SQL script: " + pathInClasspath, e);
        }
    }

}

