package dev.builder.usermanagement.domain.model;

import com.healthmarketscience.jackcess.InvalidValueException;
import dev.builder.core.domain.ValueObject;

public record AcademicInfo(AcademicQuarter quarter, QuarterGroup group) implements ValueObject {

    public AcademicInfo{
        validate(quarter, group);
    }

    public static void validate(AcademicQuarter quarter, QuarterGroup group) {
        if(!isValid(quarter,group)) {
            throw new IllegalArgumentException("Invalid Academic info");
        }
    }

    public static boolean isValid(AcademicQuarter quarter, QuarterGroup group) {
        return  quarter != null && group != null;
    }
}
