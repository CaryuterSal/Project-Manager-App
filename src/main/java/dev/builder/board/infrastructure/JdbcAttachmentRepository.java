package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.StoredFile;
import dev.builder.board.domain.model.Task;
import dev.builder.board.domain.port.out.AttachmentRepository;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.executeQuery;
import static dev.builder.core.infrastructure.persistence.CommonJdbcOperationWrappers.wrapWithConnection;

@Bean
public class JdbcAttachmentRepository extends TransactionalJdbcCrudRepository<Attachment, Attachment.Id> implements AttachmentRepository {

    private static final String INSERT = """
            INSERT INTO task_attachement(fle_id, tsk_id)
            VALUES (?, ?)
            """;

    private static final String EXISTS = """
            SELECT COUNT(*) AS total
            FROM task_attachement tc
            JOIN "FILE" f ON f.id = tc.fle_id
            WHERE tc.fle_id = ?
            AND f.active = 1;
            """;

    private static final String EXISTS_DELETED = """
            SELECT COUNT(*) AS total
            FROM task_attachement tc
            JOIN "FILE" f ON f.id = tc.fle_id
            WHERE tc.fle_id = ?
            AND f.active = 0;
            """;
    private static final String SELECT = String.format("""
            SELECT
                f.id AS %s,
                f.name as %s,
                f.mimetype as %s,
                tc.tsk_id AS %s,
            FROM task_attachement tc
            JOIN "FILE" f ON f.id = tc.fle_id
            WHERE tc.fle_id = ?
            AND f.active = 1;
            """, FileJdbcMapper.FileColumns.ID.columnName(),
            FileJdbcMapper.FileColumns.NAME.columnName(),
            FileJdbcMapper.FileColumns.MIME_TYPE.columnName(),
            FileJdbcMapper.FileColumns.ATTACHED_TO);
    private static final String SELECT_ALL = String.format("""
            SELECT
                f.id AS %s,
                f.name as %s,
                f.mimetype as %s,
                tc.tsk_id AS %s,
            FROM task_attachement tc
            JOIN "FILE" f ON f.id = tc.fle_id
            AND f.active = 1;
            """, FileJdbcMapper.FileColumns.ID.columnName(),
            FileJdbcMapper.FileColumns.NAME.columnName(),
            FileJdbcMapper.FileColumns.MIME_TYPE.columnName(),
            FileJdbcMapper.FileColumns.ATTACHED_TO);

    private static final Logger log = LoggerFactory.getLogger(JdbcAttachmentRepository.class);

    @Override
    protected Logger getLogger() {
        return log;
    }

    private JdbcStoredFileRepository storedFileRepository;

    @Inject
    public void setJdbcStoredFileRepository(JdbcStoredFileRepository storedFileRepository) {
        this.storedFileRepository = storedFileRepository;
    }

    @Inject
    public JdbcAttachmentRepository(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public Attachment save(Attachment attachment, Connection connection) {
        throw new UnsupportedOperationException("File saving is only supported with its data");
    }

    @Override
    public Attachment save(Attachment attachment, InputStream data) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::save,
                attachment,
                data
        );
    }

    @Override
    public Attachment save(Attachment attachment, InputStream data, Connection connection) {
        if(existsById(attachment.id(), connection)){
            throw new RepositoryException("Update is not supported for attachments");
        }
        if(existsDeletedById(attachment.id(), connection)){
            throw new RepositoryException("Recover is not supported for attachments");
        }
        return create(attachment, data, connection);
    }

    private Attachment create(Attachment attachment, InputStream data, Connection connection) {
        try(PreparedStatement ps  = connection.prepareStatement(INSERT)) {
            storedFileRepository.saveBaseFileInfo(attachment, data, connection);
            ps.setBytes(1, UUIDMapper.UUIDtoByteArray(attachment.id().uuid()));
            ps.setBytes(2, UUIDMapper.UUIDtoByteArray(attachment.attachedTo().value()));
            boolean updated = ps.executeUpdate() > 0;
            if (!updated) {
                throw new RepositoryException("There was a problem saving image");
            }
            return attachment;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(),e);
        }
    }

    @Override
    public boolean deleteById(Attachment.Id id, Connection connection) {
        return storedFileRepository.deleteById(id, connection);
    }

    @Override
    public List<Attachment> findAll(Connection connection) {
        return executeQuery(
                SELECT_ALL,
                PreparedStatementFiller.NO_OP,
                rs -> {
                    List<Attachment> attachments = new ArrayList<>();
                    while(rs.next()){
                        attachments.add(FileJdbcMapper.rowToAttachment(rs));
                    }
                    return attachments;
                },
                log,
                connection
        );
    }

    @Override
    public Optional<Attachment> findById(Attachment.Id id, Connection connection) {
        return executeQuery(
                SELECT,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() ? Optional.of(FileJdbcMapper.rowToAttachment(rs)) : Optional.empty(),
                log,
                connection
        );
    }

    @Override
    public Set<Attachment> findByTaskId(Task.Id taskId) {
        return wrapWithConnection(
                connectionManager,
                log,
                this::findByTaskId,
                taskId
        );
    }

    @Override
    public Set<Attachment> findByTaskId(Task.Id taskId, Connection connection) {
        return Set.of();
    }

    @Override
    public boolean existsById(Attachment.Id id, Connection connection) {
        return executeQuery(
                EXISTS,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }


    public boolean existsDeletedById(Attachment.Id id, Connection connection) {
        return executeQuery(
                EXISTS_DELETED,
                ps -> ps.setBytes(1, UUIDMapper.UUIDtoByteArray(id.uuid())),
                rs -> rs.next() && rs.getInt("total") > 0,
                log,
                connection
        );
    }
}
