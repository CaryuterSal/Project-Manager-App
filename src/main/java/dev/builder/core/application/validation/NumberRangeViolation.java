package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.properties.MessageLocalizer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Se lanza cuando el valor numérico de un campo no se encuentra dentro del rango permitido.
 */
public class NumberRangeViolation extends NumberViolation {
  public NumberRangeViolation(@NotNull MessageLocalizer messageResolver, String field, int min, int max) {
    super(field, messageResolver.getMessage("validation.field.number.range", field, min, max));
  }

  public NumberRangeViolation(@NotNull MessageLocalizer messageResolver, String field, BigDecimal min, BigDecimal max) {
    super(field, messageResolver.getMessage("validation.field.number.range.decimal", field, min, max));
  }
}
