package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;

public record AcademicQuarter(int number) implements ValueObject {

    public AcademicQuarter{
        validate(number);
    }

    public static int validate(int number) {
        if(!isValid(number)) {
            throw new IllegalArgumentException("Invalid Quarter Number");
        }
        return number;
    }

    public static boolean isValid(int number) {
        return number > 0;
    }
}
