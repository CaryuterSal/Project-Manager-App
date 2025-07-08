package dev.builder.core.infrastructure.di.runtime;

public class DefaultDependencyContainer extends AbstractDependencyContainer {

    private DefaultDependencyContainer() {
    }

    private static final class InstanceHolder {
        private static final DefaultDependencyContainer INSTANCE = new DefaultDependencyContainer();
    }

    /**
     * Getter de la clase usando el patrón <a href=https://refactoring.guru/es/design-patterns/singleton>Singleton</a>.
     * </br>
     * Es <b>Thread-safe</b>
     * @return La instancia de la clase
     */
    public static DefaultDependencyContainer getInstance(){
        return InstanceHolder.INSTANCE;
    }

    /**
     * @throws UnsupportedOperationException no está permitido el escaneo de paquetes para este tipo de contenedor de dependencias
     */
    @Override
    public void scanPackage(String packageName) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Cannot scan package for " + getClass().getSimpleName());
    }
}
