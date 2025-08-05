package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

public record AcademicQuarter(int number) implements ValueObject<AcademicQuarter> {

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

    @Override
    public int compareTo(@NotNull AcademicQuarter academicQuarter) {
        return Integer.compare(number, academicQuarter.number);
    }
}
