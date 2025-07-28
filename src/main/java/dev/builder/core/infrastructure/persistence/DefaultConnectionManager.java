package dev.builder.core.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dev.builder.core.infrastructure.di.annotation.Bean;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bean
public class DefaultConnectionManager extends BaseConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionManager.class);
    private static final String WALLET;

    static {
        try {
            WALLET = unzipWallet().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String DB_NAME = "scygsd4n0r0gz0jw_medium";
    private static final String DB_URL = "jdbc:oracle:thin:@" + DB_NAME + "?TNS_ADMIN=" + WALLET;
    private static final String DB_USER = "ADMIN";
    private static final String DB_PASSWORD = "Caematru2006#";

    private static final PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();

    static {
        try {

            System.setProperty("oracle.net.tns_admin", WALLET);
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
            connProps.setProperty("oracle.net.ssl_server_dn_match", "true");
            connProps.setProperty("fixedString", "false");
            connProps.setProperty("remarksReporting", "false");
            connProps.setProperty("restrictGetTables", "false");
            connProps.setProperty("includeSynonyms", "false");
            connProps.setProperty("defaultNChar", "false");
            connProps.setProperty("AccumulateBatchResult", "false");

            dataSource.setConnectionProperties(connProps);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public DefaultConnectionManager() {
        super(dataSource);
    }

    private static Path unzipWallet() throws IOException {
        Path tempDir = Files.createTempDirectory("oracle_wallet");
        try (InputStream zipStream = BaseConnectionManager.class.getResourceAsStream("/dev.builder/wallet/Wallet_SCYGSD4N0R0GZ0JW.zip");
             ZipInputStream zis = new ZipInputStream(zipStream)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFile = tempDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return tempDir;
    }
}
