package dev.builder.usermanagement.application.command;

import dev.builder.core.application.validation.PositiveValidator;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.usermanagement.application.validator.EmailValidator;
import dev.builder.usermanagement.application.validator.NameValidator;
import dev.builder.usermanagement.application.view.StudentView;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Invita a un nuevo estudiante para que sea parte del sistema. Este tendrá que completar su registro posteriormente usando {@link CompleteRegistrationCommand}.
 *  Posibles excepciones lanzadas:
 *   <ul>
 *       <li>{@link dev.builder.auth.application.service.UnauthorizedException} si la sesión activa no es de {@code Manager}/li>
 *       <li>Posibles violaciones de validación</li>
 *       <ul>
 *           <li>{@link dev.builder.core.application.validation.RequiredFieldViolation} si algún campo es {@code null} o solo contiene espacios</li>
 *           <li>{@link dev.builder.core.application.validation.FormatViolation} si el correo electrónico no tiene un formato válido</li>
 *           <li>{@link PositiveValidator} si el número de cuatrimestre del estudiante no es un número positivo</li>
 *       </ul>
 *   </ul>
 */
public class InviteStudentCommand extends InviteUserCommand<StudentView> {

    public enum Fields{
        EMAIL("email"),
        FIRST_NAME("firstName"),
        LAST_NAME("lastName"),
        ACADEMIC_QUARTER("academicQuarter"),
        ACADEMIC_GROUP("academicGroup");
        private final String value;
        Fields(String value) {this.value = value;}
        public String getValue() {return value;}
    }

    private final String firstName;
    private final String lastName;
    private final int academicQuarter;
    private final char academicGroup;

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public int academicQuarter() {
        return academicQuarter;
    }

    public char academicGroup() {
        return academicGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        InviteStudentCommand that = (InviteStudentCommand) o;
        return academicQuarter == that.academicQuarter && academicGroup == that.academicGroup && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(firstName);
        result = 31 * result + Objects.hashCode(lastName);
        result = 31 * result + academicQuarter;
        result = 31 * result + academicGroup;
        return result;
    }

    public InviteStudentCommand(String email, String firstName, String lastName, int academicQuarter, char academicGroup) {
        super(email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.academicQuarter = academicQuarter;
        this.academicGroup = academicGroup;
    }


    @Bean
    public static class InviteStudentCommandValidator extends InviteUserCommandValidator<InviteStudentCommand> {

        private final PositiveValidator positiveValidator;
        private final NameValidator nameValidator;

        @Inject
        public InviteStudentCommandValidator(EmailValidator emailValidator, NameValidator nameValidator, PositiveValidator positiveValidator) {
            super(LoggerFactory.getLogger(InviteStudentCommand.InviteStudentCommandValidator.class), emailValidator);
            this.positiveValidator = positiveValidator;
            this.nameValidator = nameValidator;
        }

        @Override
        protected void validateExtra(InviteStudentCommand command) {
            validate(() -> nameValidator.validate(Fields.FIRST_NAME.getValue(), command.firstName()));
            validate(() -> nameValidator.validate(Fields.LAST_NAME.getValue(), command.lastName()));
            validate(() -> positiveValidator.validate(Fields.ACADEMIC_QUARTER.getValue(), command.academicQuarter()));
        }
    }
}
