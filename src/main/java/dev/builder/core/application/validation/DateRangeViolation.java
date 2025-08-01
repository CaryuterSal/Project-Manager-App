package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * Se lanza cuando una fecha no se encuentra dentro del rango esperado.
 */
public class DateRangeViolation extends DateViolation {
  public DateRangeViolation(@NotNull MessageLocalizer messageResolver, String field, LocalDateTime before, LocalDateTime after) {
    super(field,messageResolver.getMessage("validation.field.date.range", field, before, after));
  }
}
