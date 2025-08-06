package dev.builder.core.application;

/**
 * Representa un DTO de solo lectura utilizado para presentar datos al exterior (por ejemplo, hacia una API o UI).
 *
 * <p>Un {@code View} encapsula datos agregados desde distintas fuentes y está optimizado para consulta,
 * no necesariamente refleja la estructura de las entidades de dominio.</p>
 *
 * <p>Puede ser usado como respuesta de una {@link Query} o {@link Command}.</p>
 *
 * <p>Ejemplo: {@code UserDetailsView, ProjectSummaryView}</p>
 */
public interface View {
}
