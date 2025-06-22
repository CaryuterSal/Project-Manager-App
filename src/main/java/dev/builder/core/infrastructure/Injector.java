package dev.builder.core.infrastructure;

/**
 * Encargado de la inyección de dependencias.
 * Implementa el patrón singleton para que solo exista una sola instancia de cada componente de la aplicación
 */
public class Injector {

    private static final AppContext APP_CONTEXT = new AppContext();

    /**
     * Singleton para obtener el único contexto de la aplicación
     * @return El {@link AppContext} de la aplicación
     */
    public static AppContext getContext(){
        return APP_CONTEXT;
    }
}
