package dev.builder.core.infrastructure.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;


public class ConnectionManager {


    private static final String WALLET = "";
    private static final String DB_NAME = "";
    private static final String DB_URL = "" + DB_NAME + "" + WALLET;
    private static final String DB_USER = "ADMIN";
    private static final String DB_PASSWORD = "";
    private static final String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.pool.OracleDataSource";

    public static PoolDataSource dataSource;

    static {
        try {
            dataSource = PoolDataSourceFactory.getPoolDataSource();
            dataSource.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);
            dataSource.setURL(DB_URL);
            dataSource.setUser(DB_USER);
            dataSource.setPassword(DB_PASSWORD);
            dataSource.setConnectionPoolName("JDBC_UCP_POOL");
            dataSource.setInitialPoolSize(5);
            dataSource.setMinPoolSize(5);
            dataSource.setMaxPoolSize(20);
            dataSource.setTimeoutCheckInterval(5);
            dataSource.setInactiveConnectionTimeout(10);

            Properties connProps = new Properties();
            connProps.setProperty("fixedString", "false");
            connProps.setProperty("remarksReporting", "false");
            connProps.setProperty("restrictGetTables", "false");
            connProps.setProperty("includeSynonyms", "false");
            connProps.setProperty("defaultNChar", "false");
            connProps.setProperty("AccumulateBatchResult", "false");

            dataSource.setConnectionProperties(connProps);

        } catch (Exception e) {
            System.err.println("Error inicializando el pool de conexiones:");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El pool de conexiones no fue inicializado.");
        }
        return dataSource.getConnection();
    }

}
