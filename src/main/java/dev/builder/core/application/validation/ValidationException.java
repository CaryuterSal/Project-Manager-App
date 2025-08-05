package dev.builder.core.application.validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Excepción compuesta que representa uno o más errores de validación en una solicitud.
 *
 * <p>Esta excepción se lanza cuando un {@link RequestValidator} detecta múltiples fallos
 * de validación en los campos de entrada. Cada fallo individual se encapsula como una
 * {@link FieldViolationException}.</p>
 *
 * <p>Permite acceder fácilmente a los errores específicos por nombre de campo, así como
 * obtener todos los mensajes de error agrupados.</p>
 *
 * @see FieldViolationException
 */
public class ValidationException extends Exception {
    private final List<FieldViolationException> violations;

    public  ValidationException(List<FieldViolationException> violations) {
        super("Validation failed");
        this.violations = violations;
    }

    public List<FieldViolationException> getViolations() {
        return violations;
    }

    public Optional<String> getMessageFor(String fieldName) {
        return getViolationFor(fieldName).map(FieldViolationException::getMessage);
    }

    public Optional<FieldViolationException> getViolationFor(String fieldName) {
        return violations.stream()
                .filter(v -> v.getField().equals(fieldName))
                .findFirst();
    }

    public boolean hasErrorFor(String fieldName) {
        return violations.stream().anyMatch(v -> v.getField().equals(fieldName));
    }

    public List<String> getAllMessages() {
        return violations.stream().map(FieldViolationException::getMessage).toList();
    }

    public Map<String, String> asMap() {
        return violations.stream().collect(Collectors.toMap(FieldViolationException::getField, FieldViolationException::getMessage));
    }
}
