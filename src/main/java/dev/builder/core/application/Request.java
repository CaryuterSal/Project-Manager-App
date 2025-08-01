package dev.builder.core.application;

/**
 * Representa una solicitud genérica que puede ser procesada por un {@link RequestHandler}.
 *
 * @param <R> El tipo de respuesta esperado después de procesar la solicitud.
 *
 * <p>Esta interfaz puede ser extendida por {@link Command} o {@link Query} para distinguir
 * entre solicitudes que modifican el estado del sistema (Command) y aquellas que lo consultan (Query).</p>
 */
public interface Request<R> {
}
