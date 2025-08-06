package dev.builder.core.domain;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;

public record AuditInfo(LocalDateTime createdAt, LocalDateTime updatedAt) {

    public AuditInfo{
        validate(createdAt, updatedAt);
    }

    public static void validate(LocalDateTime createdAt, LocalDateTime updatedAt) {
        if(!isValid(createdAt, updatedAt)) {
            throw new IllegalArgumentException("Invalid audit info");
        }
    }

    public static boolean isValid(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return createdAt != null && updatedAt != null && (updatedAt.isAfter(createdAt) || updatedAt.equals(createdAt));
    }
}
