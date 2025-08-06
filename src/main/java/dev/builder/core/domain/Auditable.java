package dev.builder.core.domain;

import org.apache.poi.ss.formula.functions.T;

import java.time.LocalDateTime;

public interface Auditable {

    AuditInfo auditInfo();

    default LocalDateTime createdAt(){
        requireHydrated();
        return auditInfo().createdAt();
    }

    default LocalDateTime updatedAt(){
        requireHydrated();
        return auditInfo().updatedAt();
    }

    default boolean isHydrated(){
        return auditInfo() != null;
    }

    default void requireHydrated(){
        if(!isHydrated()) {
            throw new IllegalStateException("Entity has not been hydrated");
        }
    }

    void hydrateAuditInfo(AuditInfo auditInfo);
}
