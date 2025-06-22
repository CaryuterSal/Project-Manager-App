package dev.builder.core.domain;

import dev.builder.board.domain.model.Task;

import java.util.Objects;

public interface ValueObject extends DomainObject{

     boolean equals(Object o);

    int hashCode();
}
