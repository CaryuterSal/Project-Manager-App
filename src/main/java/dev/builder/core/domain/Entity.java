package dev.builder.core.domain;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class Entity<ID> implements DomainObject {

    protected final ID id;

    public Entity(ID id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    public ID id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Entity.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .toString();
    }
}
