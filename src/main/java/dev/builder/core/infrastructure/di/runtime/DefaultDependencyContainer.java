package dev.builder.core.infrastructure.di.runtime;

public class DefaultDependencyContainer extends AbstractDependencyContainer {

    private static DefaultDependencyContainer INSTANCE;

    private DefaultDependencyContainer() {
    }
    /**
     * Getter de la clase usando el patrón <a href=https://refactoring.guru/es/design-patterns/singleton>Singleton</a>.
     * </br>
     * Es <b>Thread-safe</b>
     * @return La instancia de la clase
     */
    public static DefaultDependencyContainer getInstance(){
        if(INSTANCE == null){
            synchronized (AnnotationAwareDependencyContainer.class){
                if(INSTANCE == null){
                    INSTANCE = new DefaultDependencyContainer();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * @throws UnsupportedOperationException no está permitido el escaneo de paquetes para este tipo de contenedor de dependencias
     */
    @Override
    public void scanPackage(String packageName) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("Cannot scan package for " + getClass().getSimpleName());
    }
}
