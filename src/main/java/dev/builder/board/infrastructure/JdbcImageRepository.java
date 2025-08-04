package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.StoredFile;
import dev.builder.board.domain.model.Task;
import dev.builder.board.domain.port.out.ImageRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.wrapWithConnection;

@Bean
public class JdbcImageRepository extends TransactionalJdbcCrudRepository<Image, Image.Id> implements ImageRepository {

    private static final String INSERT = """
            INSERT INTO task_cover(fle_id, tsk_id)
            VALUES (?, ?)
            """;

    private static final String EXISTS = """
            SELECT COUNT(*) AS total
            FROM task_cover tc
            JOIN "FILE" f ON f.id = tc.fle_id
            WHERE tc.fle_id = ?
            AND f.active = 1;
            """;
    private static final String EXISTS_DELETED = """
            SELECT COUNT(*) AS total
            FROM task_cover tc
            JOIN "FILE" f ON f.id = tc.fle_id
            WHERE tc.fle_id = ?
            AND f.active = 0;
            """;
    private static final String SELECT = String.format("""
            SELECT
                f.id AS %s,
                f.name as %s,
                f.mimetype as %s,
                tc.tsk_id AS %s
            FROM task_cover tc
            JOIN "FILE" f ON f.id = tc.fle_id
            WHERE tc.fle_id = ?
            AND f.active = 1;
            """, FileJdbcMapper.FileColumns.ID.columnName(),
                FileJdbcMapper.FileColumns.NAME.columnName(),
                FileJdbcMapper.FileColumns.MIME_TYPE.columnName(),
                FileJdbcMapper.FileColumns.ATTACHED_TO.columnName());
    private static final String SELECT_ALL = String.format("""
            SELECT
                f.id AS %s,
                f.name as %s,
                f.mimetype as %s,
                tc.tsk_id AS %s
            FROM task_cover tc
            JOIN "FILE" f ON f.id = tc.fle_id
            WHERE f.active = 1;
            """, FileJdbcMapper.FileColumns.ID.columnName(),
            FileJdbcMapper.FileColumns.NAME.columnName(),
            FileJdbcMapper.FileColumns.MIME_TYPE.columnName(),
            FileJdbcMapper.FileColumns.ATTACHED_TO.columnName());

    private static final String SELECT_BY_TASK = String.format("""
            SELECT
                f.id AS %s,
                f.name as %s,
                f.mimetype as %s,
                tc.tsk_id AS %s
            FROM task_cover tc
            JOIN "FILE" f ON f.id = tc.fle_id
            JOIN task t ON t.id = tc.tsk_id AND t.active = 1
            WHERE t.id = ?
            AND f.active = 1
            """,FileJdbcMapper.FileColumns.ID.columnName(),
            FileJdbcMapper.FileColumns.NAME.columnName(),
            FileJdbcMapper.FileColumns.MIME_TYPE.columnName(),
            FileJdbcMapper.FileColumns.ATTACHED_TO.columnName());

    private static final Logger log = LoggerFactory.getLogger(JdbcImageRepository.class);
    @Override
    protected Logger getLogger() {
        return log;
    }

    private JdbcStoredFileRepository storedFileRepository;

    @Inject
    public void setStoredFileRepository(JdbcStoredFileRepository storedFileRepository) {
        this.storedFileRepository = storedFileRepository;
    }

    @Inject
    public JdbcImageRepository(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public Image save(Image image, Connection connection) {
        throw new UnsupportedOperationException("Saving image without its data is not supported");
    }

    @Override
    public Image save(Image image, InputStream data) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::save,
                image,
                data
        );
    }

    @Override
    public Image save(Image image, InputStream data, Connection connection) {
        if(existsById(image.id(), connection)){
            throw new RepositoryException("Update is not supported for images");
        }
        if(existsDeletedById(image.id(), connection)){
            throw new RepositoryException("Recover is not supported for images");
        }
        return create(image, data, connection);
    }

    private Image create(Image image, InputStream data, Connection connection) {
        try(PreparedStatement ps  = connection.prepareStatement(INSERT)) {
            storedFileRepository.saveBaseFileInfo(image, data, connection);
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(image.id().uuid()));
            ps.setBytes(2, UUIDMapper.UUIDtoByteArray(image.attachedTo().value()));
            boolean updated = ps.executeUpdate() > 0;
            if (!updated) {
                throw new RepositoryException("There was a problem saving image");
            }
            return image;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(),e);
        }
    }

    @Override
    public boolean deleteById(Image.Id id, Connection connection) {
        return storedFileRepository.deleteById(id, connection);
    }

    @Override
    public List<Image> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL,
                PreparedStatementFiller.NO_OP,
                rs -> {
                    List<Image> images = new ArrayList<>();
                    while(rs.next()){
                        images.add(FileJdbcMapper.rowToImage(rs));
                    }
                    return images;
                },
                log,
                connection
        );
    }

    @Override
    public Optional<Image> findById(Image.Id id, Connection connection) {
        return executeQuery(
                SELECT,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() ? Optional.of(FileJdbcMapper.rowToImage(rs)) : Optional.empty(),
                log,
                connection
        );
    }

    @Override
    public Optional<Image> findByTaskId(Task.Id taskId) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::findByTaskId,
                taskId
        );
    }

    @Override
    public Optional<Image> findByTaskId(Task.Id taskId, Connection connection) {
        return executeQuery(
                SELECT_BY_TASK,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(taskId.value())),
                rs -> rs.next() ? Optional.of(FileJdbcMapper.rowToImage(rs)) : Optional.empty(),
                log,
                connection
        );
    }

    @Override
    public boolean existsById(Image.Id id, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }

    public boolean existsDeletedById(Image.Id id, Connection connection) {
        return executeQuery(
                EXISTS_DELETED,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }
}
