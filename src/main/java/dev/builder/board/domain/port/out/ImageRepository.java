package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.Task;
import dev.builder.core.domain.port.TransactionalCrudRepository;

import java.sql.Connection;
import java.util.Optional;

public interface ImageRepository extends TransactionalCrudRepository<Image, Image.Id> {
    Optional<Image> findByTaskId(Task.Id taskId);
    Optional<Image> findByTaskId(Task.Id taskId,  Connection connection);
    boolean existsDeletedById(Image.Id id, Connection connection);
}
