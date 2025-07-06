package dev.builder.core.infrastructure.di.runtime;

public interface ContainerLifecycleManager {
    /**
     * Inicia de manera prematura los beans registrados antes que sean llamados por primera vez
     */
    void initialize();

    /**
     * Elimina los beans registrados del contenedor de dependencias
     */
    void clear();
}
