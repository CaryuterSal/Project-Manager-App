package dev.builder.core.application;

import dev.builder.core.application.validation.ValidationException;

/**
 * Manejador genérico para cualquier tipo de {@link Request}.
 *
 * @param <T> El tipo de solicitud.
 * @param <R> El tipo de respuesta producida al manejar la solicitud.
 */
public interface RequestHandler<T extends Request<R>, R> {
    /**
     * Maneja la solicitud especificada.
     *
     * @param request la solicitud a manejar
     * @return la respuesta correspondiente
     * @throws ValidationException si la validación del request a nivel de aplicación falla
     */
    R handle(T request) throws ValidationException;
}
