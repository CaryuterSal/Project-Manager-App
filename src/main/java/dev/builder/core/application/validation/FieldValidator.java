package dev.builder.core.application.validation;

/**
 * Representa un validador genérico para un campo individual de un formulario, comando o solicitud.
 *
 * <p>Esta interfaz define un contrato para validar un valor específico asociado a un nombre de campo.
 * En caso de que la validación falle, se lanza una {@link FieldViolationException} con información contextual
 * sobre el campo y el motivo del fallo.</p>
 *
 * <p>Se utiliza dentro de validadores más amplios (por ejemplo, {@link  RequestValidator}) para componer validaciones
 * específicas reutilizables.</p>
 *
 * @param <T> Tipo del valor que se va a validar (por ejemplo, {@code String}, {@code Integer}, {@code LocalDate}).
 *
 * @see FieldViolationException
 */
public interface FieldValidator<T>  {

    /**
     * Valida el valor proporcionado y lanza una excepción si no cumple con las reglas establecidas.
     *
     * @param fieldName Nombre lógico del campo (por ejemplo, "email", "birthDate").
     * @param value     Valor a validar (puede ser {@code null} si se espera validarlo también).
     * @throws FieldViolationException si el valor no es válido según las reglas de negocio o formato.
     */
    void validate(String fieldName, T value) throws FieldViolationException;
}
