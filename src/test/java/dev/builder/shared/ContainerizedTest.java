package dev.builder.shared;

import dev.builder.core.infrastructure.persistence.BaseConnectionManager;
import dev.builder.core.infrastructure.persistence.ConnectionManager;
import org.junit.After;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.oracle.OracleContainer;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.runOracleScript;

@IntegrationTest
public abstract class ContainerizedTest {

    static final String schemaScript = "/dev.builder/scripts/sql/schema.sql";
    static final String truncateScript = "/dev.builder/scripts/sql/truncate.sql";
    static final String insertTestDataScript = "/dev.builder/scripts/sql/insert_dummy_data.sql";
    static final String dropSchemaScript = "/dev.builder/scripts/sql/drop_tables.sql";

    static final String CONTAINER_IMAGE = "gvenzl/oracle-free:slim-faststart";
    static final  OracleContainer oracleContainer = new OracleContainer(CONTAINER_IMAGE)
            .withDatabaseName("testDatabase")
            .withUsername("ADMIN")
            .withPassword("testPassword")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("oracle-testcontainer")))
            .withReuse(true);

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
    }

    protected void executeInsertTestData(){
        runOracleScript(connectionManager, insertTestDataScript);
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
}
