package dev.builder.core.infrastructure.di;

import java.util.logging.Logger;

/**
 * Punto de entrada común que construye los componentes de la aplicación.
 * Inicializa los componentes y los vuelve accesibles durante el ciclo de vida de la aplicación
 */
public class AppContext implements DependencyContainer {

    private static final AppContext instance = new AppContext();
    private DependencyContainer annotationAwareDependencyContainer = ;
    private boolean isAnnotationAware = true;

    public void setAnnotationAware(boolean isAnnotationAware) {
        isAnnotationAware = annotationAware;
    }

    private static final Logger LOGGER = Logger.getLogger(AppContext.class.getName());

    private AppContext() {}

    public static AppContext getInstance() {
        return instance;
    }

    public static boolean register(Class<?> clazz){
        return dependencies.add(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T getDependency(Class<T> clazz){
        return (T) dependencies.stream()
                .filter(d -> d.getClass().equals(clazz))
                .findFirst()
                .orElseThrow(
                        () -> new DependencyNotFoundException(clazz)
                );
    }



}
