package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.StoredFile;
import dev.builder.board.domain.model.Task;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.persistence.UUIDGenerator;
import dev.builder.core.infrastructure.persistence.UUIDMapper;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FileJdbcMapper {

    public enum FileColumns {
        ID("id"),
        ATTACHED_TO("attached_to"),
        NAME("name"),
        MIME_TYPE("mimetype"),
        PURPOSE("purpose"),
        SOURCE("source");

        private final String columnName;

        public String columnName() {
            return columnName;
        }

        FileColumns(String columnName) {
            this.columnName = columnName;
        }
    }

    private record BaseFileInfo(UUID id, Task.Id attachedTo, String name, StoredFile.MimeType mimetype){

        private static @NotNull BaseFileInfo fromResultSet(MessageLocalizer messageLocalizer,  @NotNull ResultSet rs) throws SQLException {
            UUID id = UUIDMapper.byteArrayToUUID(rs.getBytes(FileColumns.ID.columnName));
            Task.Id attachedTo = new Task.Id(UUIDMapper.byteArrayToUUID(rs.getBytes(FileColumns.ATTACHED_TO.columnName)));
            String name = rs.getString(FileColumns.NAME.columnName);
            StoredFile.MimeType mimeType = StoredFile.MimeType.fromValue(messageLocalizer, rs.getString(FileColumns.MIME_TYPE.columnName));
            return new  BaseFileInfo(id, attachedTo, name, mimeType);
        }
    }

    public static @NotNull Image rowToImage(MessageLocalizer messageLocalizer, ResultSet rs) throws SQLException {
        BaseFileInfo baseFileInfo = BaseFileInfo.fromResultSet(messageLocalizer, rs);
        return new Image(
                new Image.Id(baseFileInfo.id()),
                baseFileInfo.attachedTo,
                new Image.Filename(baseFileInfo.name()),
                baseFileInfo.mimetype()
        );
    }

    public static @NotNull Attachment rowToAttachment(MessageLocalizer messageLocalizer, ResultSet rs) throws SQLException {
        BaseFileInfo baseFileInfo = BaseFileInfo.fromResultSet(messageLocalizer, rs);
        return new Attachment(
                new Attachment.Id(baseFileInfo.id()),
                baseFileInfo.attachedTo,
                new Attachment.Filename(baseFileInfo.name()),
                baseFileInfo.mimetype()
        );
    }

    public static @NotNull FileType extractFileType(@NotNull ResultSet rs) throws SQLException {
        return FileType.fromPurposeValue(rs.getString(FileColumns.PURPOSE.columnName));
    }
}
