package dev.builder.core.domain;


public interface ValueObject<SELF extends ValueObject<SELF>> extends DomainObject, Comparable<SELF>{

     boolean equals(Object o);

    int hashCode();
}
