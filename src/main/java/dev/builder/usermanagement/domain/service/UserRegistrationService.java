package dev.builder.usermanagement.domain.service;

import dev.builder.core.domain.DomainService;
import dev.builder.usermanagement.domain.model.*;
import org.jetbrains.annotations.NotNull;

/**
 * Servicio de dominio que coordina la lógica de registro de nuevos usuarios
 */
public class UserRegistrationService implements DomainService {

    /**
     * Un nuevo Manager se registra a nombre de un Admin, actualizando la lista interna de managers creados inmediatamente
     * @param issuer el admin que invita al manager
     * @param id el correo electrónico del nuevo manager
     * @throws NullPointerException si el correo del nuevo manager es nulo
     */
    public static void registerNewManager(@NotNull Admin issuer, Manager.Id id) {
        Manager manager = Manager.invite(issuer.id(), id);
        issuer.addCreatedManager(manager.id());
    }

    /**
     * Un nuevo estudiante se registra a nombre de un Manager, actualizando la lista interna de estudiantes creados inmediatamente
     * @param issuer el manager que invita al manager
     * @param id el correo electrónico del nuevo estudiante
     * @param name el nombre del nuevo estudiante
     * @param academicInfo la información de año y grupo del nuevo estudiante
     * @throws NullPointerException si el correo del nuevo estudiante es nulo
     */
    public static void registerNewStudent(@NotNull Manager issuer, Student.Id id, Name name, AcademicInfo academicInfo) {
        Student student = Student.invite(issuer.id(), id, name, academicInfo);
        issuer.addCreatedStudent(student.id());
    }
}
