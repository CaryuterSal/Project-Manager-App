package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.StoredFile;
import dev.builder.board.domain.port.out.StoredFileRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import dev.builder.usermanagement.domain.model.Admin;
import dev.builder.usermanagement.domain.model.Manager;
import dev.builder.usermanagement.domain.model.Student;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.infrastructure.UserJdbcMapper;
import dev.builder.usermanagement.infrastructure.UserType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.wrapWithConnection;

@Bean
public class JdbcStoredFileRepository implements StoredFileRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcStoredFileRepository.class);
    protected Logger getLogger() {
        return log;
    }

    private static final String INSERT = """
            INSERT INTO "FILE"(id, name, mimetype, purpose)
            VALUES (?, ?, ?, ?)
            """;

    private static final String INSERT_DATA = """
            INSERT INTO FILE_SOURCE(fle_id, source) VALUES (?, ?)
            """;
    private static final String DELETE = """
            UPDATE "FILE"
            SET active = 0
            WHERE id = ?
            AND active = 1
    """;
    private static final String RECOVER = """
            UPDATE "FILE"
            SET active = 1
            WHERE id = ?
            AND active = 0
            """;
    private static final String SELECT_TYPE_BY_ID = """
            SELECT purpose
            FROM "FILE"
            WHERE id = ?
            AND active = 1
            """;
    private static final String SELECT_ALL_TYPE = """
            SELECT purpose
            FROM "FILE"
            WHERE active = 1
            """;
    private static final String EXISTS = """
            SELECT COUNT(*) AS total
            FROM "FILE"
            WHERE id = ?
            AND active = 1
            """;

    private static final String SELECT_FILE_SOURCE = String.format("""
            SELECT
                fs.source as %s
            FROM "FILE" f
            JOIN file_source fs ON fs.fle_id = f.id
            WHERE f.id = ?
            AND f.active = 1
    """, FileJdbcMapper.FileColumns.SOURCE.columnName());

    private final JdbcImageRepository imageRepository;
    private final JdbcAttachmentRepository attachmentRepository;
    private final ConnectionManager connectionManager;

    @Inject
    public JdbcStoredFileRepository(ConnectionManager connectionManager, JdbcImageRepository imageRepository, JdbcAttachmentRepository attachmentRepository) {
        this.connectionManager = connectionManager;
        this.imageRepository = imageRepository;
        this.attachmentRepository = attachmentRepository;
    }

    void saveBaseFileInfo(@NotNull StoredFile<?> file, InputStream data, Connection conn) throws SQLException {
        try(PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(file.id().uuid()));
            ps.setString(2, file.name().value());
            ps.setString(3, file.mimeType().asText());
            ps.setString(4, FileType.fromDomain(file).dbValue());

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on file insert");
            createData(file.id(), data, conn);
        }
    }


    private void createData(StoredFile.Id<?> fileId, InputStream data, Connection connection) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(INSERT_DATA)){
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(fileId.uuid()));
            ps.setBinaryStream(2, data);

            boolean updated = ps.executeUpdate() > 0;
            if(!updated) throw new RepositoryException("Duplicate key on file data insert");
        }
    }

    @Override
    public boolean deleteById(StoredFile.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::deleteById,
                id
        );
    }

    @Override
    public boolean deleteById(StoredFile.Id<?> id, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(DELETE)){
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid()));
            int rs = ps.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    boolean recover(StoredFile.Id<?> id, Connection connection) {
        try(PreparedStatement ps = connection.prepareStatement(RECOVER)){
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid()));
            int rs = ps.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<? extends StoredFile<?>> findById(StoredFile.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::findById,
                id
        );
    }

    @Override
    public Optional<? extends StoredFile<?>> findById(StoredFile.Id<?> id, Connection connection) {
        return executeQuery(
                SELECT_ALL_TYPE,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() ? findSpecificFile(id.uuid(), rs, connection): Optional.empty(),
                log,
                connection
        );
    }

    @Override
    public List<? extends StoredFile<?>> findAll() {
        return wrapWithConnection(
                connectionManager,
                log,
                this::findAll
        );
    }

    @Override
    public List<? extends StoredFile<?>> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL_TYPE,
                PreparedStatementFiller.NO_OP,
                rs -> {
                    List<StoredFile<?>> files = new ArrayList<>();
                    while(rs.next()) {
                        UUID id = UUIDMapper.byteArrayToUUID(rs.getBytes(FileJdbcMapper.FileColumns.ID.columnName()));
                        findSpecificFile(id,  rs, connection).ifPresent(files::add);
                    }
                    return files;
                },
                log,
                connection
        );
    }


    private @NotNull Optional<? extends StoredFile<?>> findSpecificFile(UUID fileId, ResultSet typeResult, Connection connection) throws SQLException {
        FileType type = FileJdbcMapper.extractFileType(typeResult);
        Optional<? extends StoredFile<?>> specificFile = switch (type) {
            case ATTACHMENT -> attachmentRepository.findById(new Attachment.Id(fileId), connection);
            case TASK_COVER -> imageRepository.findById(new Image.Id(fileId), connection);
        };
        if (specificFile.isEmpty()) {
            log.warn("{} with id {} is registered on FILE table but not in child table", type.name(), fileId);
        }
        return specificFile;
    }

    @Override
    public boolean existsById(StoredFile.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::existsById,
                id
        );
    }

    @Override
    public boolean existsById(StoredFile.Id<?> id, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }

    @Override
    public Optional<InputStream> openFile(StoredFile.Id<?> id) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::openFile,
                id
        );
    }

    @Override
    public Optional<InputStream> openFile(StoredFile.Id<?> id, Connection connection) {
        return executeQuery(
                SELECT_FILE_SOURCE,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() ? Optional.of(rs.getBinaryStream(FileJdbcMapper.FileColumns.SOURCE.columnName())) : Optional.empty(),
                log,
                connection
        );
    }

}
