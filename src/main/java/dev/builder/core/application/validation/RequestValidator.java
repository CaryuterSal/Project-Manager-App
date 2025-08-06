package dev.builder.core.application.validation;

import dev.builder.core.application.Command;
import dev.builder.core.application.Query;
import dev.builder.core.application.Request;

/**
 * Encapsula la lógica de validación para una solicitud específica del sistema.
 *
 * @param <T> Tipo de {@link Request} que valida.
 *
 * <p>Debe implementarse para cada {@link Command} o {@link Query} que requiera validación de negocio
 * o estructura antes de su procesamiento.</p>
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * public class CreateUserValidator implements RequestValidator<CreateUserCommand> {
 *     public void validate(CreateUserCommand command) throws ValidationException {
 *         // Validar campos requeridos, formatos, etc.
 *     }
 * }
 * }</pre>
 */
public interface RequestValidator<T extends Request<?>>{
    void validate(T value) throws ValidationException;
}
