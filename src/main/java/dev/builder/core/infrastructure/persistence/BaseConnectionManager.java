package dev.builder.core.infrastructure.persistence;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class BaseConnectionManager implements ConnectionManager {


    protected static final String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.pool.OracleDataSource";

    private final DataSource dataSource;

    public BaseConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public BaseConnectionManager(String dbUrl, String username, String password) {
        this(dbUrl, username, password, new Properties());
    }

    public BaseConnectionManager(String dbUrl, String username, String password, Properties properties) {
        try {
            PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
            poolDataSource.setURL(dbUrl);
            poolDataSource.setUser(username);
            poolDataSource.setPassword(password);
            poolDataSource.setConnectionProperties(properties);
            poolDataSource.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);
            dataSource = poolDataSource;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection(){
        try {
            if (dataSource == null) {
                throw new SQLException("El pool de conexiones no fue inicializado.");
            }
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
