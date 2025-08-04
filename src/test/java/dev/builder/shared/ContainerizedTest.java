package dev.builder.shared;

import dev.builder.core.infrastructure.persistence.BaseConnectionManager;
import dev.builder.core.infrastructure.persistence.ConnectionManager;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import jakarta.xml.bind.DatatypeConverter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.oracle.OracleContainer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.*;

@IntegrationTest
public abstract class ContainerizedTest {

    static final String schemaScript = "/dev/builder/scripts/sql/schema.sql";
    static final String truncateScript = "/dev/builder/scripts/sql/truncate.sql";
    static final String insertTestDataScript = "/dev/builder/scripts/sql/insert_dummy_data.sql";
    static final String dropSchemaScript = "/dev/builder/scripts/sql/drop_tables.sql";

    static final String CONTAINER_IMAGE = "gvenzl/oracle-free:slim-faststart";
    static final  OracleContainer oracleContainer = new OracleContainer(CONTAINER_IMAGE)
            .withDatabaseName("testDatabase")
            .withUsername("ADMIN")
            .withPassword("testPassword")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("oracle-testcontainer")))
            .withReuse(true);


    private static String INSERT_FILE_SOURCE = """
            INSERT INTO file_source(fle_id, source) VALUES(?,?)
            """;
    private static Map<UUID, String> FILE_SOURCE_DATA = new HashMap<>();
    static {
        FILE_SOURCE_DATA.put(parseHexToUUID("E1B2C3D4E5F60123456789ABCDEF0008"), "/dev/builder/diagram.png");
        FILE_SOURCE_DATA.put(parseHexToUUID("E2B2C3D4E5F60123456789ABCDEF0009"), "/dev/builder/requirements.pdf");
    }

    protected ConnectionManager connectionManager;

    @BeforeAll
    void initContainer() {
        oracleContainer.start();
        connectionManager = new BaseConnectionManager(
                oracleContainer.getJdbcUrl(),
                oracleContainer.getUsername(),
                oracleContainer.getPassword()
        );

        executeDropSchema();
        setupFirstRun();
    }

    @AfterAll
    void stopContainer() {
    }

    protected abstract void setupFirstRun();

    @BeforeEach
    void setupDB() {
        executeSchemaScript();
        setup();
    }

    protected abstract void setup();

    @AfterEach
    void tearDownDB() {
        executeDropSchema();
        cleanup();
    }

    protected void cleanup(){}

    protected void executeInsertTestData(){
        runOracleScript(connectionManager, insertTestDataScript);
        executeInsertFiles();
    }

    protected void executeTruncateDB(){
        runOracleScript(connectionManager, truncateScript);
    }

    protected void executeSchemaScript(){
        runOracleScript(connectionManager, schemaScript);
    }

    protected void executeDropSchema(){
        runOracleScript(connectionManager, dropSchemaScript);
    }

    private void executeInsertFiles() {
        wrapWithConnection(
                connectionManager,
                LoggerFactory.getLogger(ContainerizedTest.class),
                 conn -> {
                    for(Map.Entry<UUID, String> entry : FILE_SOURCE_DATA.entrySet()){
                        try(PreparedStatement preparedStatement = conn.prepareStatement(INSERT_FILE_SOURCE)) {
                            preparedStatement.setBytes(1, UUIDMapper.UUIDtoByteArray(entry.getKey()));
                            preparedStatement.setBinaryStream(2, ContainerizedTest.class.getResourceAsStream(entry.getValue()));
                            boolean updated = preparedStatement.executeUpdate() > 0;
                        }
                    }
                    return null;
                }
        );
    }

    protected static @NotNull UUID parseHexToUUID(String hexUUID){
        byte[] bytes = DatatypeConverter.parseHexBinary(hexUUID);
        return UUIDMapper.byteArrayToUUID(bytes);
    }

}
