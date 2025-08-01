package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.ValueObject;
import org.jetbrains.annotations.NotNull;

public record AcademicInfo(AcademicQuarter quarter, QuarterGroup group) implements ValueObject<AcademicInfo> {

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

    @Override
    public int compareTo(@NotNull AcademicInfo academicInfo) {
        int cmp = quarter.compareTo(academicInfo.quarter);
        return cmp != 0 ? cmp : group.compareTo(academicInfo.group);
    }
}
