package dev.builder.core.infrastructure.properties;

import dev.builder.board.domain.model.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * Interface para traducir los nombres de las etapas (columnas) de un tablero a sus representaciones localizadas en forma de texto.
 *
 * Implementaciones de esta interfaz deben proporcionar la lógica para convertir
 * un objeto {@link Stage} en su nombre o descripción traducida, adaptada
 * al idioma o contexto requerido.
 */
@FunctionalInterface
public interface BoardStageLocalizer {
    /**
     * Traduce una etapa del tablero dada a una cadena localizada.
     *
     * @param stage la etapa del tablero a traducir; no debe ser {@code null}
     * @return la representación localizada de la etapa como {@code String}
     * @throws NullPointerException si {@code stage} es {@code null}
     */
    String translateBoardStage(@NotNull Stage stage);
}
