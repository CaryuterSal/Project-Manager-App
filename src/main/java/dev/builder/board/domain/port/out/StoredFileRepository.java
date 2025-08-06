package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.StoredFile;
import dev.builder.core.domain.port.CrudRepository;
import dev.builder.core.domain.port.TransactionalCrudRepository;
import dev.builder.core.domain.port.TransactionalDeleteRepository;
import dev.builder.core.domain.port.TransactionalReadRepository;

import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface StoredFileRepository {

    boolean deleteById(StoredFile.Id<?> id);
    boolean deleteById(StoredFile.Id<?> id, Connection connection);
    Optional<? extends StoredFile<?>> findById(StoredFile.Id<?> id);
    Optional<? extends StoredFile<?>> findById(StoredFile.Id<?> id, Connection connection);
    List<? extends StoredFile<?>> findAll();
    List<? extends StoredFile<?>> findAll(Connection connection);
    boolean existsById(StoredFile.Id<?> id);
    boolean existsById(StoredFile.Id<?> id, Connection connection);
    Optional<InputStream> openFile(StoredFile.Id<?> id);
    Optional<InputStream> openFile(StoredFile.Id<?> id, Connection connection);
}
