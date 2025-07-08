package dev.builder.core.infrastructure.di.runtime;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface BeanRetriever {
    /**
     * Obtiene la instancia con sus dependencias inyectadas e inicializado para el tipo específico de bean
     *
     * @param clazz el tipo de bean
     * @param <T>   el tipo de bean
     * @return la instancia del bean
     */
    <T> @Nullable T getInstance(Class<T> clazz);

    /**
     * Obtiene la instancia con sus dependencias inyectadas e inicializado para el nombre específico de bean.
     * Útil si hay más de un bean registrado para una determinada clase
     *
     * @param clazz    el tipo de bean
     * @param beanName el nombre de bean
     * @param <T>      el tipo de bean
     * @return la instancia del bean
     */
    <T> @Nullable T getInstance(Class<T> clazz, String beanName);

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
     * Obtiene los tipos de los beans registrados en el contenedor
     *
     * @return un {@link Set} inmodificable con las clases de los beans registrados
     */
    Set<Class<?>> getRegisteredTypes();

    /**
     * Obtiene los nombres de los beans registrados en el contenedor
     *
     * @return un {@link Set} inmodificable con las clases de los beans registrados
     */
    Set<String> getRegisteredBeanNames();
}
