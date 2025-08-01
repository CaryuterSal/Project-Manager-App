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
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.DataSourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bean
public class DefaultConnectionManager extends BaseConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionManager.class);
    private static final String WALLET;
    private static final Properties CONN_PROPS = new Properties();

    static{
        CONN_PROPS.setProperty("oracle.net.ssl_server_dn_match", "true");
        CONN_PROPS.setProperty("fixedString", "false");
        CONN_PROPS.setProperty("remarksReporting", "false");
        CONN_PROPS.setProperty("restrictGetTables", "false");
        CONN_PROPS.setProperty("includeSynonyms", "false");
        CONN_PROPS.setProperty("defaultNChar", "false");
        CONN_PROPS.setProperty("AccumulateBatchResult", "false");
        try {
            WALLET = unzipWallet().toString();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Inject
    public DefaultConnectionManager(DataSourceProperties dbProperties) {
        super(dbProperties.getDbUrl() + WALLET, dbProperties.getUser(), dbProperties.getPassword(), CONN_PROPS);
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
