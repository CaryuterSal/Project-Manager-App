package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Se lanza cuando un campo no cumple con el formato esperado.
 * Usado cuando ninguna de las otras especificaciones (Ej. {@link NumberMinViolation}, {@link DateFutureViolation}) no se ajusta a la razón del fallo de validación.
 *
 */
public class FormatViolation extends FieldViolationException {

    public FormatViolation(@NotNull MessageLocalizer messageResolver, String field, String value) {
        super(field, messageResolver.getMessage("validation.field.invalid", field, value));
    }

    public FormatViolation(@NotNull MessageLocalizer messageResolver, String field, LocalDateTime value) {
        super(field, messageResolver.getMessage("validation.field.date.invalid", field, value));
    }

    public FormatViolation(@NotNull MessageLocalizer messageResolver, String field, BigDecimal value) {
        super(field, messageResolver.getMessage("validation.field.number.invalid", field, value));
    }
}
