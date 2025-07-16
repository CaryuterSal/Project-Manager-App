package dev.builder.board.domain.model;

import com.healthmarketscience.jackcess.InvalidValueException;
import dev.builder.core.domain.ValueObject;

import java.util.Objects;


/**
 * Representa un valor numérico de orden o prioridad en el dominio.
 *
 * Este Value Object encapsula un valor {@link Double} que debe ser mayor que cero.
 */
public record Order(Double value) implements ValueObject {

    /**
     * Crea una nueva instancia de {@code Order}, validando que el valor sea positivo.
     *
     * @param value El valor de orden. No puede ser null ni menor o igual que cero.
     * @throws NullPointerException Si {@code value} es null.
     * @throws IllegalArgumentException Si {@code value} es menor o igual que cero.
     */
    public Order {
        validate(value);
    }

    /**
     * Valida que el valor dado sea mayor que cero.
     *
     * @param value Valor a validar.
     * @return El mismo valor si es válido.
     * @throws NullPointerException Si el valor es null.
     * @throws IllegalArgumentException Si el valor no es mayor que cero.
     */
    public static Double validate(Double value) {
        if (!isValid(Objects.requireNonNull(value))) {
            throw new IllegalArgumentException("Invalid order value");
        }
        return value;
    }

    /**
     * Indica si el valor es válido, es decir, no nulo y mayor que cero.
     *
     * @param value Valor a verificar.
     * @return {@code true} si el valor es válido, {@code false} en caso contrario.
     */
    public static boolean isValid(Double value) {
        return value != null && value > 0;
    }
}
