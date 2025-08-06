package dev.builder.core.domain.port;

import dev.builder.core.domain.AggregateRoot;
import dev.builder.core.domain.Entity;

public interface Repository<T extends Entity<ID>, ID>{
}
