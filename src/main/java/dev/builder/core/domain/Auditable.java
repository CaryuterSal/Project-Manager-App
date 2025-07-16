package dev.builder.core.domain;

import java.time.LocalDateTime;

public interface Auditable {

    LocalDateTime createdAt();
    LocalDateTime updatedAt();

    void hydrateAuditInfo(LocalDateTime createdAt, LocalDateTime updatedAt);
}
