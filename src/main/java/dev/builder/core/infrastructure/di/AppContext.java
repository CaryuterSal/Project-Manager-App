package dev.builder.core.infrastructure.di;

import java.util.logging.Logger;

/**
 * Punto de entrada común que construye los componentes de la aplicación.
 * Inicializa los componentes y los vuelve accesibles durante el ciclo de vida de la aplicación
 */
public class AppContext {

    private static final AppContext instance = new AppContext();
    private boolean isAnnotationAware = true;

    private static final Logger LOGGER = Logger.getLogger(AppContext.class.getName());

    private AppContext() {}

    public static AppContext getInstance() {
        return instance;
    }

}
