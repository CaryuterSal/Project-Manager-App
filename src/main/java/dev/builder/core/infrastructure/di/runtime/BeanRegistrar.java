package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.definition.BeanRegistrationConfiguration;
import dev.builder.core.infrastructure.di.definition.BeanScope;

/**
 * Maneja el registro de nuevos beans en el contenedor de dependencias
 */
public interface BeanRegistrar {
    /**
     * Registra un nuevo {@code Bean} con la configuración por defecto.
     * <ul>
     *     <li>Nombre de bean autogenerado como el nombre simple de clase en cammelCase</li>
     *     <li>{@link dev.builder.core.infrastructure.di.definition.InstantiationMode#EAGER} modo</li>
     *     <li>sin {@link dev.builder.core.infrastructure.di.definition.InitCustomizer}</li>
     *     <li>{@link BeanScope#SINGLETON} como tipo.</li>
     * </ul>
     *
     * @param clazz la clase del bean a registrar
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     */
    <T> boolean register(Class<T> clazz);

    /**
     * Registra un nuevo {@code Bean}
     * <p>
     * El nombre del bean se define por default al nombre simple de la clase en camelCase
     *
     * @param config la información del como debe ser registrado el bean
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     */
    <T> boolean register(BeanRegistrationConfiguration<T> config);

    /**
     * Busca dentro del paquete candidatos para ser registrados como {@code Beans}.
     * Si se encuentran, entonces se registran y se inyectan sus dependencias en caso de ser necesario
     *
     * @param packageName nombre del paquete relativo al context path donde se buscan candidatos
     */
    void scanPackage(String packageName);
}
