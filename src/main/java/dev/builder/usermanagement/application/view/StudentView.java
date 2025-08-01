package dev.builder.usermanagement.application.view;

import java.time.LocalDateTime;
import java.util.Objects;

public class StudentView extends UserView{
    private final String firstName;
    private final String lastName;
    private final int quarter;
    private final char group;

    public StudentView(String email, boolean verified, LocalDateTime createdAt, LocalDateTime updatedAt, String firstName, String lastName, int quarter, char group) {
        super(email, verified, createdAt, updatedAt);
        this.firstName = firstName;
        this.lastName = lastName;
        this.quarter = quarter;
        this.group = group;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public int quarter() {
        return quarter;
    }

    public char group() {
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StudentView that = (StudentView) o;
        return quarter == that.quarter && group == that.group && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(firstName);
        result = 31 * result + Objects.hashCode(lastName);
        result = 31 * result + quarter;
        result = 31 * result + group;
        return result;
    }
}
