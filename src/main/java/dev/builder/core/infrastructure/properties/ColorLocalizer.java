package dev.builder.core.infrastructure.properties;

import dev.builder.board.domain.model.Color;
import org.jetbrains.annotations.NotNull;

/**
 * Interface para traducir colores a sus representaciones localizadas en forma de texto.
 *
 * Implementaciones de esta interfaz deben proporcionar la lógica para convertir
 * un objeto {@link Color} en su nombre o descripción traducida según el idioma o
 * contexto deseado.
 */
@FunctionalInterface
public interface ColorLocalizer {

    /**
     * Traduce un color dado a una cadena localizada.
     *
     * @param color el color a traducir; no debe ser {@code null}
     * @return la representación localizada del color como {@code String}
     * @throws NullPointerException si {@code color} es {@code null}
     */
    String translateColor(@NotNull Color color);
}
