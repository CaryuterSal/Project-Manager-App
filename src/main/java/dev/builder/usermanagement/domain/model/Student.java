package dev.builder.usermanagement.domain.model;

import dev.builder.core.domain.AuditInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student extends User<Student.Id>{

    private final Manager.Id createdBy;
    private Name name;
    private AcademicInfo academicInfo;

    public Student(Manager.Id createdBy, Student.Id id, String password, Name name, AcademicInfo academicInfo, boolean verified) {
        super(id, password, verified);
        this.createdBy = createdBy;
        this.academicInfo = academicInfo;
        this.name = name;
    }

    private Student(Manager.Id issuer, Student.Id id, Name name, AcademicInfo academicInfo){
        super(id);
        this.createdBy = Objects.requireNonNull(issuer);
        this.name = Objects.requireNonNull(name);
        this.academicInfo = Objects.requireNonNull(academicInfo);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull Student invite(Manager.Id issuer, Student.Id id, Name name, AcademicInfo academicInfo) {
        return new Student(issuer, id, name, academicInfo);
    }


    @Override
    public Student hydratedWithAuditInfo(AuditInfo auditInfo){
        this.hydrateAuditInfo(auditInfo);
        return this;
    }

    public Manager.Id createdBy() {
        return createdBy;
    }

    public Name name() {
        return name;
    }

    public void setName(Name name) {
        this.name = Objects.requireNonNull(name);
    }

    public AcademicInfo academicInfo() {
        return academicInfo;
    }

    public void setAcademicInfo(AcademicInfo academicInfo) {
        this.academicInfo = Objects.requireNonNull(academicInfo);
    }

    public static class Id extends User.Id {

        public Id(String email) {
            super(email);
        }

        public static String validate(String email){
            if(!isValid(email)){
                throw new IllegalArgumentException("Invalid email");
            }
            return email;
        }

        public static boolean isValid(String email) {
            return Email.isValid(email);
        }
    }
}
