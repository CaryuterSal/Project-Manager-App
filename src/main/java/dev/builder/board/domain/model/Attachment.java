package dev.builder.board.domain.model;

import java.io.InputStream;

/**
 * Represent a file that is attached in the description of tasks for documentation purposes
 */
public class Attachment extends StoredFile{

    public Attachment(Filename filename, MimeType mimeType) {
        super(filename, mimeType);
    }

    public Attachment(Id id, Filename filename, MimeType mimeType) {
        super(id, filename, mimeType);
    }
}
