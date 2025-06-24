package dev.builder.core.infrastructure.di;

import javafx.beans.property.BooleanProperty;
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
     * @param clazz el tipo del bean
     * @param provider el proveedor de instancias nuevas
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean register(Class<T> clazz, Provider<T> provider);


    /**
     * Registra un nuevo {@code Bean} con {@link BeanScope#PROTOTYPE} como tipo.
     *
     * El nombre del bean se define por default al nombre simple de la clase en camelCase
     * @param clazz el tipo del bean
     * @param beanName el nombre del bean que puede servir para buscarlo más tarde
     * @param provider el proveedor de instancias nuevas
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean register(Class<T> clazz, String beanName, Provider<T> provider);

    /**
     * Registra un nuevo {@code Bean} con {@link BeanScope#SINGLETON} como tipo.
     *
     * El nombre del bean se define por default al nombre simple de la clase en camelCase
     * @param clazz el tipo del bean
     * @param instance la instancia que se asigna como única para el singleton
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean registerSingleton(Class<T> clazz, T instance);


    /**
     * Registra un nuevo {@code Bean} con {@link BeanScope#SINGLETON} como tipo.
     *
     * El nombre del bean se define por default al nombre simple de la clase en camelCase
     * @param clazz el tipo del bean
     * @param beanName el nombre del bean que se puede usar más tarde para buscarlo en el registro
     * @param instance la instancia que se asigna como única para el singleton
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean registerSingleton(Class<T> clazz, String beanName, T instance);


    /**
     * Registra un nuevo {@code Bean} con {@link BeanScope#SINGLETON} como tipo. Defiere la creación de la instancia al usar un {@link Provider}
     *
     * El nombre del bean se define por default al nombre simple de la clase en camelCase
     *
     * @param clazz el tipo del bean
     * @param provider el proveedor de la instancia que se asigna como única para el singleton
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean registerSingleton(Class<T> clazz, Provider<T> provider);


    /**
     * Registra un nuevo {@code Bean} como {@link BeanScope#SINGLETON} como tipo. Define un nombre en específico para el bean.
     *
     * Defiere la creación de la instancia al usar un {@link Provider}
     *
     * @param clazz el tipo del bean
     * @param beanName el nombre del bean que sirve como referencia para buscarlo más tarde
     * @param provider el proveedor de la instancia que se asigna como única para el singleton
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean registerSingleton(Class<T> clazz, String beanName, Provider<T> provider);

    /**
     * Registra un nuevo {@code Bean} con un determinado scope (tipo de ciclo de vida)
     * @param clazz el tipo del bean
     * @param provider factory para beans. Si es de tipo singleton este se usará una única vez al ser llamado
     * @param scope la estrategia de creación de bean
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean register(Class<T> clazz, Provider<T> provider, BeanScope scope);

    /**
     * Registra un nuevo {@code Bean} con un determinado nombre y scope (tipo de ciclo de vida)
     * @param clazz el tipo del bean
     * @param beanName el nombre del bean que sirve como referencia para buscarlo más tarde
     * @param provider factory para beans. Si es de tipo singleton este se usará una única vez al ser llamado
     * @param scope la estrategia de creación de bean
     * @return si el bean se registró como nuevo, falso si ya existía en el contenedor
     * @param <T> tipo del bean
     */
    <T> boolean register(Class<T> clazz, String beanName, Provider<T> provider, BeanScope scope);

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
     * Elimina los beans registrados del contenedor de dependencias
     */
    void clear();

    /**
     * Obtiene los tipos de los beans registrados en el contenedor
     * @return un {@link Set} inmodificable con las clases de los beans registrados
     */
    Set<Class<?>> getRegisteredTypes();

}
