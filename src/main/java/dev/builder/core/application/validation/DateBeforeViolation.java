package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * Se lanza cuando una fecha no es anterior al valor requerido.
 */
public class DateBeforeViolation extends DateViolation {
    public DateBeforeViolation(@NotNull MessageLocalizer messageResolver, String field, LocalDateTime date) {
        super(field,messageResolver.getMessage("validation.field.date.before", field, date));
    }

}
