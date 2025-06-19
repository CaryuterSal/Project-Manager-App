package dev.builder.core.domain;

public abstract class LocalEntity<ID> extends Entity<ID>{

    public LocalEntity(ID id) {
        super(id);
    }
}
