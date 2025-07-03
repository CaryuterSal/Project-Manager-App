package dev.builder.core.infrastructure.di.definition.context;

/**
 * Define cuál es el comportamiento en el que un bean {@link BeanScope#SINGLETON} se instancia
 */
public enum InstantiationMode {
    LAZY,
    EAGER
}
