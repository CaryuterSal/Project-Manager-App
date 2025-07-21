package dev.builder.board.domain.port.out;

import dev.builder.board.domain.model.StoredFile;
import dev.builder.core.domain.port.CrudRepository;

import java.io.InputStream;

public interface StoredFileRepository extends CrudRepository<StoredFile, StoredFile.Id> {

    InputStream openFile(StoredFile.Id id);

    InputStream openFile(StoredFile file);
}
