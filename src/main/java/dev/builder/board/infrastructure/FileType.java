package dev.builder.board.infrastructure;

import dev.builder.board.domain.model.Attachment;
import dev.builder.board.domain.model.Image;
import dev.builder.board.domain.model.StoredFile;
import org.jetbrains.annotations.NotNull;

public enum FileType {
    TASK_COVER("cover"),
    ATTACHMENT("attachement");
    private final String purposeValue;
    FileType(String purposeValue) {
        this.purposeValue = purposeValue;
    }
    public String dbValue() {
        return purposeValue;
    }

    public static FileType fromDomain(StoredFile<?> domainEntity){
        if(domainEntity instanceof Image){
            return FileType.TASK_COVER;
        } else if(domainEntity instanceof Attachment){
            return FileType.ATTACHMENT;
        }
        throw new IllegalArgumentException("Unknown file type");
    }

    public static @NotNull FileType fromPurposeValue(String purposeValue) {
        for (FileType fileType : FileType.values()) {
            if (fileType.purposeValue.equals(purposeValue)) {
                return fileType;
            }
        }
        throw new IllegalArgumentException("Unknown purposeValue: " + purposeValue);
    }
}
