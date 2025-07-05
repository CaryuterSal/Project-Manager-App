package dev.builder.core.infrastructure.di.runtime;

import dev.builder.core.infrastructure.di.definition.BeanRegistrationConfiguration;
import dev.builder.core.infrastructure.di.definition.BeanScope;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface DependencyContainer {

    /**
     * Obtiene la instancia con sus dependencias inyectadas e inicializado para el tipo específico de bean
     * @param clazz el tipo de bean
     * @return la instancia del bean
     * @param <T> el tipo de bean
     */
    <T> @Nullable T getInstance(Class<T> clazz);

    /**
     * Obtiene la instancia con sus dependencias inyectadas e inicializado para el nombre específico de bean
     * @param beanName el nombre de bean
     * @return la instancia del bean
     * @param <T> el tipo de bean
     */
    <T> @Nullable T getInstance(String beanName);

    /**
     * @param clazz el tipo de bean
     * @return true si el tipo es {@link BeanScope#SINGLETON}
     */
    boolean isSingleton(Class<?> clazz);

    /**
     * @param beanName el nombre del bean
     * @return true si el tipo es {@link BeanScope#SINGLETON}
     */
    boolean isSingleton(String beanName);

    /**
     * Registra un nuevo {@code Bean} con {@link BeanScope#PROTOTYPE} como tipo.
     *
     * El nombre del bean se define por default al nombre simple de la clase en camelCase
     * @param config la información del como debe ser registrado el bean
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     */
    <T> boolean register(BeanRegistrationConfiguration<T> config);

    /**
     * Busca dentro del paquete candidatos para ser registrados como {@code Beans}.
     * Si se encuentran, entonces se registran y se inyectan sus dependencias en caso de ser necesario
     * @param packageName nombre del paquete relativo al context path donde se buscan candidatos
     */
    void scanPackage(String packageName);

    /**
     * Inicia de manera prematura los beans registrados antes que sean llamados por primera vez
     */
    void initialize();

    /**
     * @param type el tipo del bean
     * @return true si el bean está registrado en el contenedor
     */
    boolean isRegistered(Class<?> type);

    /**
     * @param beanName el nombre del bean
     * @return true si el bean está registrado en el contenedor
     */
    boolean isRegistered(String beanName);

    /**
     * Elimina los beans registrados del contenedor de dependencias
     */
    void clear();

    /**
     * Obtiene los tipos de los beans registrados en el contenedor
     * @return un {@link Set} inmodificable con las clases de los beans registrados
     */
    Set<Class<?>> getRegisteredTypes();

    /**
     * Obtiene los nombres de los beans registrados en el contenedor
     * @return un {@link Set} inmodificable con las clases de los beans registrados
     */
    Set<String> getRegisteredBeanNames();

}
