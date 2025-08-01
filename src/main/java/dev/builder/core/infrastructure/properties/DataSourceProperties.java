package dev.builder.core.infrastructure.properties;

/**
 * Obtiene las propiedades para conectividad a la base de datos
 */
public interface DataSourceProperties {
    /**
     * Obtiene la URL {@code JDBC} de conectividad a la base de datos. Puede o no estar completa
     * @return la URL
     */
    String getDbUrl();

    /**
     * Obtiene el nombre de usuario para inicio de sesión con credenciales simples
     * @return el nombre de usuario
     */
    String getUser();

    /**
     * Obtiene la contraseña para inicio de sesión con credenciales simples
     * @return la constraseña del usuario
     */
    String getPassword();

    /**
     * Obtiene el nombre del schema o base de datos, en caso de que exista alguno, sino, devuelve {@code null}
     * @return el nombre del schema
     */
    String getDbName();
}
