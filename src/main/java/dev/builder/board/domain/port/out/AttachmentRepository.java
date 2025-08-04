package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.Task;
import dev.builder.core.domain.port.TransactionalCrudRepository;

import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Set;

public interface AttachmentRepository extends TransactionalCrudRepository<Attachment, Attachment.Id> {
    Set<Attachment> findByTaskId(Task.Id taskId);
    Set<Attachment> findByTaskId(Task.Id taskId,  Connection connection);
    boolean existsDeletedById(Attachment.Id id, Connection connection);
    Attachment save(Attachment attachment, InputStream data);
    Attachment save(Attachment attachment, InputStream data, Connection connection);
}
