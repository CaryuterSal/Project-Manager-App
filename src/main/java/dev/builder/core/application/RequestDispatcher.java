package dev.builder.core.application;

import dev.builder.core.application.validation.ValidationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Enruta y ejecuta mensajes por medio de sus respectivos {@link RequestHandler}
 *
 * <p>Este dispatcher es genérico y puede usarse para manejar cualquier tipo de
 *  * {@link Request}, delegando la lógica de negocio al {@link RequestHandler}
 *  * correspondiente que haya sido previamente registrado.</p>
 **/
public class RequestDispatcher {

    protected final Map<Class<?>, RequestHandler<?, ?>> handlers = new HashMap<>();

    /**
     * Registra un handler específico para un tipo de request.
     *
     * @param requestType la clase del request que será manejado por el handler.
     * @param handler     la implementación de {@link RequestHandler} que manejará el request.
     * @param <R>         el tipo del request, que debe extender {@link Request}.
     * @param <V>         el tipo del resultado devuelto por el handler.
     */
    public <R extends Request<V>, V> void registerHandler(Class<R> requestType, RequestHandler<R,V> handler){
        handlers.put(requestType, handler);
    }

    /**
     * Ejecuta un request, buscando el handler correspondiente y delegando su procesamiento.
     *
     * @param request el request a procesar.
     * @param <R>     el tipo del request.
     * @param <V>     el tipo del valor devuelto.
     * @return el resultado de ejecutar el handler asociado al request.
     * @throws ValidationException si ocurre algún error de validación durante el procesamiento.
     * @throws IllegalArgumentException si no hay un handler existente para
     */
    @SuppressWarnings("unchecked")
    public <R extends Request<V>, V> V dispatch(R request) throws ValidationException{
        RequestHandler<R, V> handler = (RequestHandler<R, V>) handlers.get(request.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for command: " + request.getClass());
        }
        return handler.handle(request);
    }
}
