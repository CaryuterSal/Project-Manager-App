package dev.builder.core.domain;

public abstract class AggregateRoot<ID> extends Entity<ID>{
    public AggregateRoot(ID id) {
        super(id);
    }
}
