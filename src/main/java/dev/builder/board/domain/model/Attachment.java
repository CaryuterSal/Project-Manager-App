package dev.builder.board.domain.model;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.UUID;

/**
 * Represent a file that is attached in the description of tasks for documentation purposes
 */
public class Attachment extends StoredFile<Attachment.Id> {

    public Attachment(Id id, Task.Id attachedTo, Filename filename, MimeType mimeType) {
        super(id, attachedTo, filename, mimeType);
    }

    public static class Id extends StoredFile.Id<Id> {
        public Id(UUID uuid) {
            super(uuid);
        }

        @Override
        public int compareTo(@NotNull Id id) {
            return super.compareTo(id);
        }
    }
}
